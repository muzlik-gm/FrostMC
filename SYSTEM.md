# Fragment Powers — Full Sub-Ability & Block Manipulation Compendium (Single File)
> Complete, developer-ready specification of **all 10 Fragments**, every sub-ability (including locked/unlocked per rank), **per-rank variations**, **VFX layering**, **particle sets**, **animation notes**, **environment behavior**, **block-manipulation mechanics** (vectors, speed/acceleration curves, impact logic), **lifetimes**, **sound cues**, and **damage attribution rules**. Everything in one place for direct plugin implementation (BendersMC / ProjectKorra style).

---

## Table of Contents
1. Global rules & systems
2. Activation & slot mapping
3. Fragment table & unlocking rules
4. Universal VFX layering specification
5. Block-manipulation engine (armor-stand controller + falling blocks + speed curves)
6. Per-fragment: full sub-ability lists and *every rank* variation
   - Fire, Water, Air, Earth, Dark, Light, Void, Mob, Dragon, Storm
7. Damage attribution & entity ownership
8. Lifetimes, cleanup, and registry
9. Example YAML snippet (per-ability per-rank)
10. Implementation notes & performance caps

---

## 1) Global rules & systems (always enforced)
- **All spawned things are tracked** in an `ActiveEffectRegistry` keyed by owner UUID: type, id, startTime, scheduledEndTime, linkedEntities (armorstands, fallingblocks), configFlags.
- **No permanent world grief**: earth/terrain manipulations must be packet-only (recommended) or `temp=true` with guaranteed safe revert and protection against tile-entity/residual side effects.
- **Projectiles & effects** always carry `fragmentOwner` metadata (UUID) and `abilityId` to attribute damage and events.
- **Activation**: player selects hotbar 0..4 then uses `Sneak+RightClick` (primary) or `Sneak+LeftClick` (alternate). Plugin resolves `activeFragment` → `abilities[slot]` → requirements (mana, level, rank, cooldown).
- **Rank unlocking**: each fragment has a `BaseRank` and `MaxRank`; ability slots beyond base are locked and unlocked at specified ranks (defined per fragment below).
- **Mana, cooldowns & scaling**: abilities use the global mana formulas (configurable). Rank modifies damage, mana cost, particle intensity, durations, and speed curves.
- **Performance caps**: global `particleDensityMultiplier` to throttle heavy VFX; high-rank VFX must check server load and fall back to simpler visuals if threshold exceeded.

---

## 2) Activation & slot mapping
- Hotbar → slot mapping:
  - Hotbar 0 → Slot 0 (Primary) — always available
  - Hotbar 1 → Slot 1 (Secondary) — always available
  - Hotbar 2 → Slot 2 (Ultimate) — always available
  - Hotbar 3 → Slot 3 (Advanced) — rank-locked (unlock thresholds defined per fragment)
  - Hotbar 4 → Slot 4 (Mastery) — rank-locked (unlock thresholds defined per fragment)
- Activation inputs:
  - `Sneak + Right-Click` → activates selected slot in forward-facing direction; uses raycast to 20 blocks by default for target detection.
  - `Sneak + Left-Click` → mirrored or alternate activation for same slot (allowing secondary behaviors).
- UI: minimal actionbar messages for feedback, bossbar only for rituals or multi-stage casts.

---

## 3) Fragment table & general unlocking rules
| Fragment | Base Rank | Max Rank | Max Abilities |
|----------|-----------|----------|---------------|
| Fire     | 3         | 8        | 5 |
| Water    | 2         | 4        | 3 |
| Air      | 3         | 8        | 5 |
| Earth    | 2         | 6        | 4 |
| Dark     | 4         | 9        | 5 |
| Light    | 4         | 9        | 5 |
| Void     | 7         | 9        | 4 |
| Mob      | 5         | 7        | 4 |
| Dragon   | 8         | 10       | 4 |
| Storm    | 6         | 8        | 4 |

- **Unlock rule convention** (applies unless fragment-specific override given):
  - `BaseRank` fragments start with slots 0..2 active.
  - Slot 3 unlock threshold = `BaseRank + 2` (unless specified).
  - Slot 4 unlock threshold = `BaseRank + 4` (unless specified, and clipped to MaxRank).
- For fragments with small MaxRank: some locks may be permanently unavailable (e.g., Water base 2 max 4 - may unlock slot3 at R4 only; slot4 unavailable).
- User note: you confirmed "for 3, yes to all" — Fire and Air (BaseRank 3) will be designed to be able to unlock all available slots through rank progression (R5/R7 unlocks).

---

## 4) Universal VFX layering (applies to every single ability)
Each ability's visual design must be built from these layers. Implementation must render them in this order (Core → Secondary → Ambient → Impact → Cinematic), with precise timing.

1. **Core (Primary) layer**
   - Purpose: identity of ability (e.g., the fireball body, the ice shard).
   - Particle choices: `FLAME`, `DRIP_WATER`, `SNOW_SHOVEL`, `CLOUD`, `END_ROD`, `DRAGON_BREATH`, or `dust` helpers for block types.
   - Movement: tied to controller entity or center of ability.
   - Rank scaling: count, brightness, size.

2. **Secondary (Trail / accent)**
   - Purpose: motion trails and streaks to convey speed/direction.
   - Particle choices: `END_ROD`, `SOUL_FIRE_FLAME`, `SPELL_WITCH`, `SWEEP_ATTACK`.
   - Behavior: follow core with delay, interpolate across ticks for smooth lines.
   - Rank scaling: density + colored gradients (two-tone fade).

3. **Ambient (fill / environment)**
   - Purpose: background ambience for longer-lasting effects (aura, fog).
   - Particle choices: `SMOKE_LARGE`, `SMOKE_NORMAL`, `SPELL_INSTANT`, `DRIP_LAVA/WATER`.
   - Behavior: persists around area, low density to avoid lag.

4. **Impact (on-hit / collision)**
   - Purpose: burst, shard, spark at collision or area-of-effect.
   - Particle choices: `LAVA`, `ITEM_CRACK`, `BLOCK_CRACK`, `EXPLOSION_NORMAL`, `HEART` (for heals).
   - Behavior: spawn at impact point, radial velocity, short lifetime (0.2–0.8s).

5. **Cinematic (optional — slow-time or camera effect)**
   - Purpose: short, dramatic frame for ultimates (0.2–0.8s), may include screen tint or camera pull (simulate via player velocity/hit effects). Use sparingly for PvP fairness.
   - Implementation: server-applied small motion impulse or blindness/fade for affected players; do not force camera control.

**Timing & layering rules**
- Core + Secondary: spawn on cast start; trails update each tick.
- Ambient: spawn as ongoing effect for duration D (use `ActiveEffectRegistry`).
- Impact: spawn on collision; propagate damage and apply status.
- Cinematic: trigger at peak moment (e.g., ultimate detonation), limited by config to avoid spam.

---

## 5) Block-manipulation engine (detailed)
Used for Earth primary terrain moves and for projectile-like "blocks" (boulder, ice shard shells). System components:

### A. Controller entity (ArmorStand projectile)
- Attributes:
  - Invisible, marker armorstand (no gravity), no bounding-box rendering (or small), ticks with server scheduler.
  - Holds metadata: `ownerUUID`, `abilityId`, `lifeTicks`, `maxLifeTicks`, `attachedShellType`.
- Movement:
  - Each tick compute `velocity` using direction vector and acceleration curve:
    ```text
    velocity = direction.normalize() * (baseSpeed + ticksActive * accelerationFactor)
    ```
  - BaseSpeed & Accel examples:
    - Earth boulder: base=1.3 b/t, accel=0.05 → limits to 2.9 b/t max
    - Ice shard: base=1.0 b/t, accel=0.08 → limits 2.4 b/t
    - Air blade: base=1.6 b/t, accel=0.1 → limits 3.2 b/t
    - Fire wave: base=2.0 b/t, accel=0.06 → limits 3.0 b/t
  - Use interpolation (lerp) for smooth direction changes (player aim tracking or homing).

### B. Visual shell (FallingBlock or Particle shell)
- Two modes:
  1. **FallingBlock**: real entity using server physics. Use only where safe & minimal; must be flagged `tempFalling=true`; store original block type to revert if placed.
  2. **Particle shell**: preferred. Render block-like visuals using `BLOCK_CRACK` or `dust`/`item_crack` particles at armorstand position to simulate a moving block. Cheaper and safer.
- Collision detection:
  - Raycast or bounding-box overlap with entities each tick.
  - On collision: run `onImpact` (damage, apply status, spawn impact VFX, remove shell).

### C. Impact logic & physics
- On impact compute:
  - `damage = baseDamage * (1 + 0.15 * fragmentRank)`
  - `knockbackVector = normalize(targetLocation - impactLocation) * knockbackStrength`
  - `applyDamageWithAttribution(playerUUID, entity, damage)`
- For environmental impacts (boulder hitting ground), optionally spawn debris particles & small temp blocks (packet-only) that last `0.6–1.2s`.
- For "trap" abilities (earth pillar): spawn invisible armor-stand hitbox that pushes players upward or stuns; also spawn packet-only pillar visuals.

### D. Persistence & cleanup
- Each controller keeps `maxLifeTicks` (e.g., 40–120 ticks). On expiration, spawn `endImpact` VFX and remove.
- If owner logs out or server stops, forcibly remove all controllers owned by that UUID immediately.
- Record spawned controllers in `ActiveEffectRegistry` for auditability.

---

## 6) Per-fragment — ALL sub-abilities, per-rank details, VFX layering, block manipulation details
> For each fragment: list slots 0..4 (some fragments have capped number). For each sub-ability include: brief name, type, full per-rank parameter table where relevant (R1..Rmax), exact VFX layer composition, animation notes, block-manipulation details (if any), sound cues, lifetime, cooldown, mana cost, and damage attribution.

> **Convention for per-rank tables:** show base / Rank+1 / Rank+2 ... until MaxRank. When a fragment cannot reach some rank, stop at MaxRank. Where slot is locked at certain rank, note unlock rank.

---

### FIRE (BaseRank 3 → MaxRank 8) — MaxAbilities 5
**Unlocks:** slot3 unlocks at R5, slot4 at R7.

#### Slot 0 — Flame Burst (Primary Projectile/AoE)
- **Type:** projectile_aoe (explodes on impact)
- **Base parameters (R3 baseline):**
  - Mana: 20
  - Cooldown: 5s
  - BaseDamage: 6 (hearts: 3 hearts = 6 HP)
  - Range: 12 blocks
  - LifeTicks: 60 (3s)
- **Per-rank scaling** (multiplicative + additive):
  - Damage = BaseDamage * (1 + 0.12 * (Rank - BaseRank))
  - Mana = round(Mana * (1 + 0.10*(Rank-BaseRank)))
  - Explosion radius = 3 + 0.3*(Rank-BaseRank) (clamped)
- **VFX Layers:**
  1. **Core:** Spiral `FLAME` sphere around central point; rotation speed = 720deg/sec baseline, increases 6% per rank.
  2. **Secondary:** `END_ROD` ember stream trailing behind orbit, count=6 * (1 + 0.08*(Rank-BaseRank))
  3. **Ambient:** `SMOKE_LARGE` low-density ring; spawn per 0.1s tick.
  4. **Impact:** radial `LAVA` + `EXPLOSION_NORMAL` + `BLOCK_CRACK:NETHERRACK` (if using packets) — small chunks simulated.
  5. **Cinematic:** brief (0.15s) heat-shimmer scale and slight camera blur for victims within 1.5 blocks (implemented via short blindness/fade effect).
- **Animation timeline:**
  - 0.0s cast: brief charge (0.2s) with `CRIT` sparks.
  - 0.0–3.0s: projectile moves; secondary trail updates each tick (interpolated).
  - On impact: spawn impact layer, deal damage, ignite `ON_FIRE` status (5s).
- **Environment:** ground magma decal packet (radius = explosion radius) visible to players within 32 blocks; decal auto-clear after 2.5s.
- **Damage Attribution:** projectile `fragmentOwner` metadata → on collision call `dealDirectDamage(player, target, computedDamage)`.

#### Slot 1 — Blazing Step (Dash & Trail)
- **Type:** self-teleport/velocity + area trail damaging
- **Unlock:** active from start
- **Parameters baseline (R3):**
  - Mana: 30
  - Cooldown: 8s
  - DashDistance: 8 blocks
  - TrailDuration: 3s
  - TrailDamage per tick: 1.5 (per 0.5s)
- **Per-rank effects:**
  - DashDistance increases +1 block per rank.
  - TrailDamage += 0.5 per rank, TrailDuration +0.4s per rank (cap 6s).
- **VFX Layers:**
  1. **Core:** linear `FLAME` streak following player during dash (particle line rendered with 1.2px spacing).
  2. **Secondary:** `SMOKE_NORMAL` behind streak with fading alpha gradient.
  3. **Ambient:** ember sprites randomly spawn along path at low density.
  4. **Impact:** on enemy hit spawn `SMALL_FLAME` burst + small knockback puff.
- **Implementation details:**
  - Movement: apply temporary velocity (server-side) rather than teleport for smoother animation: `player.setVelocity(direction*speed)` with speed curve: instant peak then quick damp (ease-out).
  - Trail hit detection: spawn invisible armor-stands every 0.6 blocks with hitbox radius 0.6; each armorstand lives TrailDuration and checks collisions every 0.25s.
  - Auto-clean: ensure trail armorstands removed at expiry or on logout.

#### Slot 2 — Inferno Maelstrom (Vortex AoE)
- **Type:** area_pull + damage over time
- **Parameters baseline (R3):**
  - Mana: 60
  - Cooldown: 20s
  - Radius: 6 blocks
  - PullDuration: 3s
  - TickDamage: 1.0 per 0.5s
- **Rank scaling:**
  - PullStrength = 0.6 + 0.12*(Rank-BaseRank) (units of blocks/tick toward center)
  - TickDamage scales +15% per rank
- **VFX Layers:**
  1. **Core:** swirling vertical pillar of `FLAME` (concentric rings) — ring count scales with rank.
  2. **Secondary:** `LAVA` specks and `END_ROD` embers rising.
  3. **Ambient:** low-density smoke fog (makes area look hot).
  4. **Impact:** when an enemy reaches center, small `EXPLOSION_NORMAL` + knockback outward.
  5. **Cinematic:** optional (config) brief screen tremor for nearby players (0.15s)
- **Mechanics & animation:**
  - Pull: each tick, for each entity in radius compute `dirToCenter` vector and apply velocity `dirToCenter.normalize() * pullStrength`, clamped to prevent ragdolling.
  - Damage: periodic calls to `dealDirectDamage` with owner metadata.
  - Environment: ground magma decal (packet-only) appears and fades 1s after vortex ends.
- **Safety:** cap entities affected to `maxEntitiesInEffect` config value to avoid server strain.

#### Slot 3 — Phoenix Rebirth (Unlocked R5)
- **Type:** revive/aoe damage on trigger
- **Trigger modes:** manual cast or on lethal hit (configurable)
- **Parameters baseline (R5):**
  - Mana: 80 (on manual), or auto-consume on lethal
  - Cooldown: 60s
  - ReviveHP: 50% max
  - AoERadius: 4 blocks
  - KnockbackForce: 0.8 to 1.6 per rank increment
- **VFX Layers:**
  1. **Core:** phoenix wings shape made with `CRIT` + `FLAME` arcs (precomputed particle path in frame sequence) — wings spread over 0.6s.
  2. **Secondary:** upward ember column `LAVA` + `END_ROD`.
  3. **Ambient:** falling ash (low density).
  4. **Impact:** radial flame burst on completion.
  5. **Cinematic:** 0.3s slow-motion for local player (use small server-side delay for movement inputs—careful to not cause anti-cheat triggers).
- **Lifetime:** visual sequence 4s; revival occurs at 50% point (1.8s).
- **Damage & attribution:** AoE damage credited to owner.

#### Slot 4 — Eternal Flame (Unlocked R7)
- **Type:** transformation buff
- **Params (R7 baseline):**
  - Mana: 100
  - Cooldown: 90s
  - Duration: 15s
  - FireDamageBoost: +30% base
  - ManaCostReduction: -50% on fire abilities
- **VFX Layers:**
  1. **Core:** full-body flowing `FLAME` aura (dense)
  2. **Secondary:** ember ribbons trailing movement
  3. **Ambient:** glow halo `END_ROD`
  4. **Cinematic:** subtle color tint on screen for the player only
- **Animation:** body particle anchors around player bones (head, shoulders, hands, feet)
- **Environment:** none (visual-only)
- **Notes:** careful with anti-cheat/motion if providing gliding; use controlled movement mode.

---

### EARTH (BaseRank 2 → MaxRank 6) — MaxAbilities 4
**Unlocks:** slot3 unlock at R4 (Terra Shaper). Earth is the canonical block manipulation fragment — this section details safe, high-speed block movement.

#### Earth-specific global rules
- **Allowed earth blocks** (config): STONE, COBBLESTONE, ANDESITE, DIORITE, GRANITE, DIRT, GRASS_BLOCK, SAND, GRAVEL, NETHERRACK (admin configurable).
- **Prefer packet-only visuals** for moving blocks. If using real block placement, do `temp=true`, lock changes in a transaction, schedule revert, and prevent tile-entity side-effects.
- **Use armor-stand controllers** and `BLOCK_CRACK:blocktype` particle shells for most moving visuals.
- **When visually ripping blocks from ground**: spawn small falling block debris (particle-based) and an armorstand to simulate the core mass.

#### Slot 0 — Stone Fist (Primary Melee)
- **Type:** empower melee hit
- **Base (R2):**
  - Mana: 20
  - Cooldown: 5s
  - ExtraDamage: +2.5 (hearts = 1.25 HP?) — define system units clearly (we use HP units where 1 heart = 2 HP)
- **VFX Layers:**
  1. **Core:** quick `BLOCK_DUST` using stone particle with short range
  2. **Secondary:** shock ripple `SWEEP_ATTACK`
- **Animation:** quick arm slam with ground particle burst
- **Block manip:** none (just dust)

#### Slot 1 — Earthen Fortress (Temporary wall)
- **Type:** defensive wall
- **Base (R2):**
  - Mana: 30
  - Cooldown: 10s
  - WallSize: 3x2 blocks, in front of player
  - Duration: 10s
- **VFX Layers:**
  1. **Core:** rising stone shards rendered as `BLOCK_CRACK:STONE` with arc trajectories
  2. **Secondary:** dust pool at base `BLOCK_DUST`
- **Implementation (recommended): packet-only):
  - Send `sendBlockChange` for each client within view range to render wall blocks.
  - Spawn invisible armor-stand hitboxes (or `slime` hitbox) to block movement & projectiles server-side.
  - If real blocks must be used, place and mark as `temp=true` and schedule revert on expiry.
  - If destroyed by player, auto-regenerate (if config allows) until duration ends.
- **Collision:** hitboxes handle collisions & sound generation for hits.
- **Lifetime:** 10s

#### Slot 2 — Seismic Slam (Ultimate Shockwave)
- **Type:** AoE radial shock with temporary uplift illusion
- **Base (R2):**
  - Mana: 50
  - Cooldown: 16s
  - Radius: 7 blocks
  - StunDuration: 2s
- **VFX Layers:**
  1. **Core:** radial `BLOCK_DUST` ring expanding outward (concentric rings at 0.15s intervals)
  2. **Secondary:** `ITEM_CRACK` for pebbles launched outward
  3. **Ambient:** subtle ground rumble (sound)
- **Block manipulation (illusion):**
  - Create short-lived raised-ledge illusion by sending `sendBlockChange` to elevate 1–2 rows of block texture (visual only) for 0.8s.
  - Optionally spawn falling block debris in ring using particle shells.
- **Impact:** radial push vector computed as `push = (entityPos - center).normalize() * pushStrength`, falloff = `1 - (distance/radius)`, apply stun and damage appropriately.
- **Lifetime:** visual 0.8s + temp effects.

#### Slot 3 — Terra Shaper (Advanced, unlock R4)
- **Type:** create ridge/ramp (configurable shapes)
- **Base (R4):**
  - Mana: 60
  - Cooldown: 30s
  - RampLength: 4–8 blocks depending on rank
  - Duration: 6→12s (scales with rank)
- **VFX Layers:**
  1. **Core:** sweeping `BLOCK_CRACK` and `BLOCK_DUST` showing block mass slide upward
  2. **Secondary:** particulate dust trail along ramp
- **Block manipulation mechanics:**
  - Preferred approach: spawn a grid of packet-only block changes to show the new ramp, and spawn corresponding armor-stand colliders along ramp path to allow traversal or blocking.
  - Movement animation: block visuals use an ease-in / accelerate-out curve across `durationAnim` (0.4s–0.9s): position(t) = start + (end - start) * (t/durationAnim)² (ease-in squared) for a solid visual feel.
  - If using fallingblocks: spawn FallingBlock entities at start with velocity vector up & forward; fallingblocks flagged to not drop items on landing and to be removed upon landing into a placeholder (or removed entirely and replaced with packet visuals).
- **Lifetime & revert:** ensure full revert to original block state at `t + Duration`.

---

### WATER (BaseRank 2 → MaxRank 4) — MaxAbilities 3
**Unlocks:** no slot3 (MaxAbilities 3); all 3 available by ranking (BaseRank 2 ensures core 3 available, but slot2 ultimate may be stronger on R4).

#### Slot 0 — Aqua Pulse (Heal/AoE)
- **Type:** AoE heal + small cleanse
- **Base (R2):**
  - Mana: 25
  - Cooldown: 8s
  - HealOverTime: 2 hearts over 5s at R2
- **Per-rank:** healing amount + radius scale with rank (R3/R4 more potent)
- **VFX Layers:**
  1. **Core:** `WATER_SPLASH` spherical ring, animated outward over 0.6s
  2. **Secondary:** rising `DRIP_WATER` + soft light `END_ROD` glints
  3. **Ambient:** translucent blue ripple (transparent)
- **Environment:** packet-only water-sheen decal; no actual water placement.

#### Slot 1 — Tidal Shield (Barrier)
- **Type:** damage-absorption barrier
- **Base (R2):**
  - Mana: 35
  - Cooldown: 12s
  - AbsorbHP: 12 HP (6 hearts)
  - Duration: 8s
- **VFX Layers:** vertical `BUBBLE_COLUMN` wall with `SPLASH` accent
- **Collision:** server-side block of projectiles via armor-stand colliders.

#### Slot 2 — Tsunami Wave (Ultimate)
- **Type:** sweeping wave push + slow
- **Base (R2):**
  - Mana: 55
  - Cooldown: 18s
  - PushDistance: up to 10 blocks
  - SlowDuration: 4s
- **VFX Layers:**
  1. **Core:** forward-moving `WATER_WAKE` particles in a broad arc
  2. **Secondary:** `SPLASH` and water spray particles
  3. **Impact:** hit splash + `DRIP_WATER` shower on targets
- **Block manipulation:** spawn water-spray decals along path (packet-only)

---

### AIR (BaseRank 3 → MaxRank 8) — MaxAbilities 5
**Unlocks:** slot3 unlock R5, slot4 unlock R7 (per earlier "for 3 yes to all").

#### Slot 0 — Wind Blade (Primary)
- **Type:** piercing projectile (multi-hit)
- **Base (R3):**
  - Mana: 18
  - Cooldown: 4s
  - Damage: 5 hearts (scales)
  - PiercingCount: 1 + floor((Rank-BaseRank)/2)
- **VFX Layers:**
  1. **Core:** narrow `CLOUD` streak elongated into blade shape
  2. **Secondary:** `END_ROD` sparks along leading edge
  3. **Impact:** `SWEEP_ATTACK` particle on hit
- **Motion:** straight linear, speed 1.6→3.2 b/t depending on rank & acceleration
- **Block manip:** none

#### Slot 1 — Gale Step (Mobility)
- **Type:** instant dash + short speed buff
- **Params:** DashDistance 10→14 based on rank; Speed buff 4s→6s
- **VFX:** gust ring on step using `CLOUD`, dust swirls
- **Env:** none

#### Slot 2 — Tempest Barrage (Ultimate)
- **Type:** volley of wind blades
- **Params:** fires 6–8 blades over 0.8–1.2s
- **VFX Layers:**
  1. **Core:** multiple small `CLOUD` blades fired in sequence
  2. **Secondary:** thin trailing ribbons (`END_ROD`)
  3. **Cinematic:** small staggered slow-motion for final strike (optional)
- **Lifetime:** projectiles despawn after 2.5s if no hit

#### Slot 3 — Cyclone Armor (R5)
- **Type:** rotating defensive field
- **Params:** Duration 8s, deflects arrows, deals contact damage
- **VFX Layers:** rotating ring of `CLOUD` + `END_ROD` with inward gust lines
- **Env:** none

#### Slot 4 — Storm Sovereign (R7)
- **Type:** transformation granting flight and major speed buffs
- **Params:** Duration 12s, instant-cast abilities or cooldown reductions
- **VFX:** full-body wind ribbons, dynamic particle ribbons anchored to bone points
- **Env:** none

---

### DARK (BaseRank 4 → MaxRank 9) — MaxAbilities 5
**Unlocks:** slot3 R6, slot4 R8 (typical for high-tier fragment).

#### Slot 0 — Shadow Strike
- **Type:** ranged dark bolt + wither
- **Base (R4):**
  - Mana: 22
  - Cooldown: 6s
  - Damage: base 4 hearts
  - Debuff: Wither II for 6s
- **VFX Layers:**
  1. **Core:** smoky purple bolt (`SMOKE_LARGE` tinted)
  2. **Secondary:** brief trail `SPELL_WITCH`
  3. **Impact:** small shadow burst + particle distortion
- **Env:** none

#### Slot 1 — Vampiric Drain
- **Type:** beam that steals health
- **Base (R4):**
  - Mana: 40
  - Cooldown: 12s
  - DrainAmount: up to 5 hearts, caster heals 70% of damage
- **VFX Layers:**
  1. **Core:** thin tether beam (black/purple) connecting caster & target
  2. **Secondary:** swirling particles sucked into caster
  3. **Impact:** healing pulse visual at caster
- **Mechanics:** attach healing amount to `dealDirectDamage` call.

#### Slot 2 — Abyssal Void (Ultimate)
- **Type:** domain creation — heavy debuffs in area
- **Base (R4):**
  - Mana: 70
  - Cooldown: 25s
  - Radius: 8 blocks
  - Duration: 10s
  - Effects: Wither III, Slowness III, Weakness II (configurable)
- **VFX Layers:**
  1. **Core:** dense purple fog (`SMOKE_LARGE`) with animated swirl
  2. **Secondary:** distortion rings (`PORTAL` particles)
  3. **Ambient:** low hum sound and muffled visuals for players inside
- **Env:** visual only; apply periodic damage via owner attribution.

#### Slot 3 — Shadow Clone (R6)
- **Type:** summon decoys that mimic attacks
- **Params:** spawn 1–2 clones for 12–15s, clones deal 40–60% of caster damage
- **VFX:** translucent hue silhouettes with echo step sounds
- **Mechanics:** clones are fake entities with owner metadata; must be limited to avoid server strain.

#### Slot 4 — Eternal Darkness (R8)
- **Type:** domain/transform — invis & huge damage boost
- **Params:** 15s duration; massive damage bonus + life-steal on hits
- **VFX:** full-body shadow overlay, particles fading in/out; heavy ambience sound

---

### LIGHT (BaseRank 4 → MaxRank 9) — MaxAbilities 5
**Unlocks:** slot3 R6, slot4 R8

#### Slot 0 — Radiant Lance
- **Type:** piercing beam
- **Base (R4):**
  - Mana: 25
  - Cooldown: 5s
  - Damage: 6 hearts
  - Effect: Blind target 4s on hit
- **VFX Layers:**
  1. **Core:** narrow luminous beam (`END_ROD` + `WHITE`)
  2. **Secondary:** sparkle glints
  3. **Impact:** flash + temporary light bloom (packet-only)
- **Env:** none

#### Slot 1 — Divine Blessing
- **Type:** heal & cleanse
- **Base (R4):**
  - Mana: 35
  - Cooldown: 10s
  - Heal: 5 hearts
- **VFX:** sunrise ring with soft chime; `END_ROD` particles in ring
- **Env:** none

#### Slot 2 — Celestial Judgment (Ultimate)
- **Type:** sky pillar AOE
- **Base (R4):**
  - Mana: 65
  - Cooldown: 20s
  - Radius: 7 blocks
- **VFX:** descending bright pillar, white explosion on strike, crack thunder sound
- **Damage attribution:** owner for every strike.

#### Slot 3 — Holy Sanctuary (R6)
- **Type:** zone heal/damage hybrid
- **Params:** heals allies, damages enemies inside
- **VFX:** glowing white zone with pulsing particles

#### Slot 4 — Seraph Ascension (R8)
- **Type:** transformation: flight + double heal
- **VFX:** angelic wings, radiant aura, ascending sound cue

---

### VOID (BaseRank 7 → MaxRank 9) — MaxAbilities 4
**Unlocks:** slot3 may be R8 depending on design (MaxAbilities 4 total).

#### Slot 0 — Void Slash
- **Type:** true-damage projectile (ignores armor partially)
- **Base (R7):**
  - Mana: 32
  - Cooldown: 5s
  - Damage: high true-damage baseline
- **VFX:** warped purple sliver (portal particles), visual tear trail
- **Effect:** bypass armor by configurable percent

#### Slot 1 — Blink Step
- **Type:** teleport + rift spawn
- **Base (R7):**
  - Mana: 35
  - Cooldown: 8s
  - Range: up to 15 blocks
  - RiftDamage: small hitbox for 3s after teleport
- **VFX Layers:** portal swirl, suction sound
- **Mechanics:** spawn invisible hitbox entity at origin/destination to handle effects

#### Slot 2 — Dimensional Collapse (R8 or R9)
- **Type:** gravity well
- **Params:** Mana 85, cooldown 28s, large pull, heavy damage at end, stun
- **VFX:** deep purple whirl with heavy bass; use sparingly

#### Slot 3 — Spatial Manipulation (optional R9)
- **Type:** advanced space warp (telefrag mechanics optional and dangerous — implement only with strict caps)
- **VFX:** multiple layered portal rings with oscillating particles

---

### MOB (BaseRank 5 → MaxRank 7) — MaxAbilities 4
**Unlocks:** slot3 at R6 (Legion)

#### Slot 0 — Beast Summon
- **Type:** summons 3 wolves or modded companions
- **Base (R5):**
  - Mana: 30
  - Cooldown: 8s
  - SummonDuration: 30s
- **VFX:** green motes + animal call sound
- **Mechanics:** summons carry owner metadata and limited loot behavior

#### Slot 1 — Iron Golem Guardian
- **Type:** heavy tank summon
- **Base:** Mana 45, Cooldown 15s, Duration 45s
- **VFX:** construct forming particles, heavy stomp sound

#### Slot 2 — Legion of Shadows (R6)
- **Type:** mass summon
- **Base:** Mana 80, Cooldown 30s, Duration 60s
- **Mechanics:** spawn many small temporary minions with owner metadata (limit cap)

#### Slot 3 — Pack Call (optional)
- **Type:** tactical summon buff (rage or defense buff on minions)

---

### STORM (BaseRank 6 → MaxRank 8) — MaxAbilities 4
**Unlocks:** slot3 maybe R7 or as design requires.

#### Slot 0 — Lightning Bolt
- **Type:** targeted instant strike
- **Base (R6):**
  - Mana: 28
  - Cooldown: 4s
  - Damage: high instant
- **VFX:** bright flash + crack; use fake lightning for servers where real lightning is restricted
- **Damage attribution:** ensure mapping to owner (lightning events sometimes attribute differently).

#### Slot 1 — Chain Lightning
- **Type:** chains between targets
- **Params:** arcs up to 5 enemies, damage falloff per jump
- **VFX:** thin arcs with `ELECTRIC_SPARK` style particle (or `SWEEP_ATTACK`)

#### Slot 2 — Thunder God's Wrath (Ultimate)
- **Type:** repeated strikes in area
- **Params:** mana 70, cooldown 22s, duration few seconds with multiple strikes
- **VFX:** rolling storm cloud particles, multiple flash & rumble

#### Slot 3 — Storm Surge (optional)
- **Type:** electric field creation, slows movement and shocks on contact

---

### DRAGON (BaseRank 8 → MaxRank 10) — MaxAbilities 4
**Unlocks:** high-tier fragment; slot availability limited to 4 but abilities are very powerful.

> Note: Dragon fragment acquisition should be gated (dragon scales custom item + dragon head). This fragment's abilities are cinematic and heavy on VFX but limited in count.

#### Slot 0 — Dragon's Roar (Cone breath)
- **Type:** cone AoE breath
- **Base (R8):**
  - Mana: 40
  - Cooldown: 6s
  - ConeWidth: 12 blocks
- **VFX Layers:** `DRAGON_BREATH` core, secondary ember shower, cinematic roar audio

#### Slot 1 — Draconic Wings (Flight)
- **Type:** temporary flight + speed
- **Params:** Mana 50, Duration 15s
- **VFX:** spectral wings, feather/scale particles anchored to shoulders

#### Slot 2 — Cataclysm (R9)
- **Type:** transform into dragon form (avatar) for 10s
- **Effects:** major damage amp, AOE breath always active, damage immunity scaling
- **VFX:** massive storm of particles, ground rumble, cinematic camera q

#### Slot 3 — Dragon Meteor (optional)
- **Type:** meteor call down — falling block meteor (packet or fallingblock with temp handling)
- **Danger:** use careful spawn limits; meteor should be fallingblock entity `tempFalling=true` with owner metadata

---

## 7) Damage attribution & plugins compatibility
- **Direct damage**: call `Entity#damage(dmg, Player)` where available or fire a custom `EntityDamageByEntityEvent` setting `damager = player`.
- **Projectile damage**: set projectile shooter to player if possible and set `PersistentDataContainer` key `fragmentOwner=uuid`.
- **Indirect environmental damage** (burn ticks, lingering fields): spawn small invisible damage-source entity with metadata `ownerUUID`, intercept `EntityDamageEvent`, and if damage source matches that entity map to owner.
- **Summon kills & drops**: summons should carry `ownerUUID` so that killed-by-minion XP/drops can be credited or disabled per config.

---

## 8) Lifetimes, cleanup & registry
- All effects spawn entries in `ActiveEffectRegistry` with:
  - `id` (UUID), `ownerUUID`, `abilityId`, `type`, `spawnTime`, `scheduledEndTime`, `linkedEntities[]`.
- On player logout or server shutdown: iterate registry and cancel all entries where `ownerUUID == loggedOutUUID`.
- Auto-save registry state every 60s (volatile) and persist only essential state to allow safe cleanup.
- Spawned controllers & shells must have `maxLifeTicks` and removal must free their memory & entity handles.

---

## 9) Example YAML (per-ability per-rank expanded)
```yaml
FIRE:
  slot-0:
    id: flame_burst
    name: "Flame Burst"
    base:
      mana: 20
      cooldown: 5
      damage: 6.0
      range: 12
      lifeTicks: 60
    perRank:
      R4:
        damageMultiplier: 1.12
        manaMultiplier: 1.10
      R5:
        damageMultiplier: 1.24
        manaMultiplier: 1.20
    vfx:
      core: FLAME
      secondary: END_ROD
      ambient: SMOKE_LARGE
      impact: LAVA
    block_changes: []
    damage_attribution: owner_uuid
