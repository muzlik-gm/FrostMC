# 🌟 FrostSMP Fragment System - Quick Guide

**Version:** 1.0.0 | **Minecraft:** 1.20.4 | **Author:** muzlik-gm

---

## 📖 What are Fragments?

Fragments are elemental powers that give you special abilities. You can only have ONE Fragment active at a time. Each Fragment has 3-5 unique abilities, levels (1-50), and ranks (1-8).

---

## 🌈 The 10 Fragments

| Fragment | Rank | Theme | Abilities |
|----------|------|-------|-----------|
| 💧 Water | 2 | Healing/Support | 3 |
| 🪨 Earth | 2 | Defense/Tank | 3 |
| 🔥 Fire | 3 | Offensive Damage | 5 |
| 🌪️ Air | 3 | Mobility/Speed | 5 |
| 🌑 Dark | 4 | Life-steal/Debuffs | 5 |
| ☀️ Light | 4 | Holy/Healing | 5 |
| 🐺 Mob | 5 | Summoning | 3 |
| ⚡ Storm | 6 | Lightning | 3 |
| 🕳️ Void | 7 | Space-time | 3 |
| 🐉 Dragon | 8 | Ultimate Power | 5 |

**Note:** Rank 2 Fragments (Water, Earth) cannot unlock slots 3 and 4.

---

## 🎯 How to Get a Fragment

### New Player Starter Fragment
**New players automatically get a fragment on first join!**
- Default: Water Fragment (configurable)
- Auto-activated (ready to use immediately)
- Welcome message explains the system
- Can be disabled in config

### Fragment Creation Ritual
1. Craft the Fragment Creation item (see CRAFTING_RECIPES.md)
2. Place it on the ground and right-click
3. Stay within 12 blocks for 3 minutes
4. Your Fragment is now charged

### Fragment Changer Ritual
1. Craft Fragment Changer item
2. Place and right-click to start ritual
3. Stay within 10 blocks for 2.5 minutes
4. Select which Fragment to activate
5. 1 hour cooldown between uses

---

## 📈 Progression System

### Fragment Levels (1-50)
- Reduces cooldowns by up to 30%
- Reduces mana costs by up to 30%
- Increases damage by up to 30%
- Gain XP by killing mobs and bosses

### Fragment Ranks (1-8)
- Unlocks new ability slots
- Increases max mana by 50 per rank
- Fragments start at different ranks
- Rank up through rituals

### Character Levels (1-30)
- Increases max mana capacity
- Level 1: 160 mana
- Level 30: 450 mana
- Separate from Fragment levels

---

## ⚡ Mana System

**Max Mana = 160 + (Character Level × 10) + (Fragment Rank × 50)**

**Regeneration:** 1 mana/sec + (0.1 × Fragment Level)

**Disable Mana:** Set `mana_system_enabled: false` in config.yml for free abilities

---

## 🎮 Controls

**Default:**
- Scroll Wheel: Cycle abilities
- Number Keys (1-5): Select ability slot
- Right-Click: Use selected ability
- `/controls`: Change control scheme

**Action Bar Display:**
```
✦ LVL: 15/30 │ ꜰɪʀᴇ ⚡100/250 [■] ○ ○ ✗ ✗
```
- `[■]` = Selected ability (ready)
- `■` = Ready ability
- `5s` = Cooldown remaining
- `○` = Locked (need to unlock)
- `✗` = Empty slot

---

## 🔥 Fire Fragment Abilities

**Slot 0:** Flame Burst - Fireball (6 hearts, 20 mana, 5s CD)
**Slot 1:** Blazing Step - Dash 8 blocks (30 mana, 8s CD)
**Slot 2:** Inferno Maelstrom - Fire tornado (10 hearts, 60 mana, 20s CD)
**Slot 3:** Phoenix Rebirth - Revive on death (80 mana, 1 hour CD) [Rank 4]
**Slot 4:** Eternal Flame - Become flame (100 mana, 90s CD) [Rank 5]

---

## 💧 Water Fragment Abilities

**Slot 0:** Aqua Pulse - Heal sphere (8 hearts, 25 mana, 8s CD)
**Slot 1:** Tidal Shield - Absorb 12 hearts (35 mana, 12s CD)
**Slot 2:** Tsunami Wave - Knockback wave (4 hearts, 55 mana, 18s CD)

---

## 🌪️ Air Fragment Abilities

**Slot 0:** Wind Blade - Piercing projectile (10 hearts, 18 mana, 4s CD)
**Slot 1:** Gale Step - Dash 10 blocks (25 mana, 6s CD)
**Slot 2:** Tempest Barrage - 8 wind blades (48 hearts total, 65 mana, 22s CD)
**Slot 3:** Cyclone Armor - Reflect projectiles (45 mana, 15s CD) [Rank 4]
**Slot 4:** Storm Sovereign - Fly 10 minutes (90 mana, 75s CD) [Rank 5]

---

## 🪨 Earth Fragment Abilities

**Slot 0:** Stone Fist - Heavy punch (14 hearts, 20 mana, 5s CD)
**Slot 1:** Earthen Fortress - +8 armor (30 mana, 10s CD)
**Slot 2:** Seismic Slam - Ground shockwave (12 hearts, 50 mana, 16s CD)

---

## 🌑 Dark Fragment Abilities

**Slot 0:** Shadow Strike - Wither bolt (8 hearts, 22 mana, 6s CD)
**Slot 1:** Vampiric Drain - Life-steal (10 hearts, 40 mana, 12s CD)
**Slot 2:** Abyssal Void - Debuff zone (70 mana, 25s CD)
**Slot 3:** Shadow Clone - 2 clones (55 mana, 20s CD) [Rank 5]
**Slot 4:** Eternal Darkness - Invisible vampire (95 mana, 80s CD) [Rank 6]

---

## ☀️ Light Fragment Abilities

**Slot 0:** Radiant Lance - Light beam (12 hearts, 25 mana, 5s CD)
**Slot 1:** Divine Blessing - Heal + cleanse (10 hearts, 35 mana, 10s CD)
**Slot 2:** Celestial Judgment - Sky pillar (16 hearts, 65 mana, 20s CD)
**Slot 3:** Holy Sanctuary - Healing zone (50 mana, 18s CD) [Rank 5]
**Slot 4:** Seraph Ascension - Become angel (100 mana, 85s CD) [Rank 6]

---

## ⚡ Storm Fragment Abilities

**Slot 0:** Lightning Bolt - Instant strike (14 hearts, 28 mana, 4s CD)
**Slot 1:** Chain Lightning - Chains to 5 enemies (10 hearts each, 40 mana, 10s CD)
**Slot 2:** Thunderstorm Descent - 10 strikes (12 hearts each, 75 mana, 24s CD)

---

## 🕳️ Void Fragment Abilities

**Slot 0:** Void Slash - True damage (16 hearts, 32 mana, 5s CD)
**Slot 1:** Blink Step - Teleport 15 blocks (35 mana, 8s CD)
**Slot 2:** Dimensional Collapse - Black hole (20 hearts, 85 mana, 28s CD)

---

## 🐺 Mob Fragment Abilities

**Slot 0:** Beast Summon - 3 wolves (30 mana, 8s CD)
**Slot 1:** Iron Golem Guardian - Summon golem (45 mana, 15s CD)
**Slot 2:** Legion of Shadows - Undead army (80 mana, 30s CD)

---

## 🐉 Dragon Fragment Abilities

**Slot 0:** Dragon's Roar - Dragon breath (18 hearts, 40 mana, 10s CD)
**Slot 1:** Draconic Wings - Fly 30 minutes (50 mana, 18s CD)
**Slot 2:** Cataclysm - Dragon form (100 mana, 120s CD)
**Slot 3:** Dragonic Fury - 5 homing fireballs (80 mana, 30s CD) [Rank 9]
**Slot 4:** Dragon Ascension - True dragon (150 mana, 120s CD) [Rank 10]

---

## 🔴 Admin Fragment

**Access:** `/frag forceactivate <player> admin` (Admin only)

**9 Destructive Abilities:**
- Slot 0: Reality Tear (100 hearts)
- Slot 1: Omnipresent Blink (100 block teleport)
- Slot 2: Cataclysm (80 hearts, 25 block radius)
- Slot 3: Void Chains (Immobilize all)
- Slot 4: Annihilation Beam (20 hearts/tick)
- Slot 5: Temporal Freeze (Freeze time)
- Slot 6: Meteor Storm (15 meteors)
- Slot 7: Execution (Instant death)
- Slot 8: Apocalypse (160 hearts, 60 block radius)

**All abilities cost 0 mana. Hidden from GUIs.**

---

## �  Commands

### Player Commands
- `/fragment` or `/frag` - View Fragment info
- `/frag list` - List available Fragments
- `/frag activate <type>` - Activate charged Fragment
- `/controls` - Change control scheme

### Admin Commands
- `/frag give <player> <type>` - Give Fragment
- `/frag forceactivate <player> <type>` - Force activate
- `/frag setlevel <player> <level>` - Set level
- `/frag setrank <player> <rank>` - Set rank
- `/frag addxp <player> <amount>` - Add XP
- `/frag reload` - Reload config
- `/frag info <player>` - View player data

---

## 🔮 Rituals

### Fragment Creation
**Duration:** 3 minutes | **Range:** 12 blocks
**Materials:** See CRAFTING_RECIPES.md

### Fragment Changer
**Duration:** 2.5 minutes | **Range:** 10 blocks | **Cooldown:** 1 hour
**Materials:** 4 Ender Pearls, 8 Gold Ingots

### Rank-Up Ritual
**Duration:** 45 seconds
**Materials:** (Rank + 2) Diamonds, (Rank + 1) Emeralds, +Netherite at Rank 5+

### Ability Expansion (Unlock Slot 3)
**Duration:** 2 minutes
**Materials:** 1 Nether Star, 1 Totem, 2 Diamond Blocks + Fragment items

### Mastery Expansion (Unlock Slot 4)
**Duration:** 3 minutes
**Materials:** 2 Nether Stars, 1 Beacon, 1 Elytra, 2 Netherite Blocks + Fragment items

---

## ⚙️ Configuration

### Key Settings (config.yml)

```yaml
mana_system_enabled: true          # Enable/disable mana

character:
  max_level: 30
  base_mana: 160
  mana_per_level: 10.0

mana:
  base_regen_rate: 1.0
  level_regen_bonus: 0.1

fx:
  particle_density: 0.5            # 0.5 = 50% particles
  sound_volume: 1.0

vfx:
  enable_performance_throttling: true

admin_fragment:
  enabled: true
  break_blocks: false
  broadcast_apocalypse: true

new_player:
  give_starter_fragment: true      # Give new players a fragment
  starter_fragment_type: "water"   # Which fragment to give
  auto_activate: true              # Auto-activate (no ritual needed)
  welcome_message: true            # Send welcome message
```

---

## 🏆 Boss XP Rewards

- Ender Dragon: 500 XP
- Wither: 300 XP
- Warden: 400 XP
- Elder Guardian: 150 XP

---

## 🎯 Quick Tips

**Beginners:** Start with Water or Earth (Rank 2, easy)
**Intermediate:** Try Fire or Air (more abilities)
**Advanced:** Dark, Light, or Storm (high skill)
**Endgame:** Void or Dragon (ultimate challenge)

**Combat Tips:**
- Watch your mana bar
- Learn ability combos
- Keep mana flasks in inventory
- Know enemy Fragment weaknesses

---

## 🛠️ Technical Info

**Requirements:**
- Minecraft 1.20.4+
- Paper/Spigot server
- Java 21

**Installation:**
1. Place JAR in `plugins/` folder
2. Restart server
3. Configure `config.yml`
4. Use `/frag reload`

**Data Storage:** `plugins/FrostSMP/data/` (JSON files)

---

## 🐛 Troubleshooting

**Abilities not working?**
- Check mana (action bar)
- Verify cooldowns
- Confirm Fragment is active: `/frag`

**No particles?**
- Check `particle_density` in config
- Enable particles in client settings

**Ritual not starting?**
- Verify all materials present
- Check 1-minute failure cooldown

**Lost Fragment?**
- Fragments never lost, only deactivated
- Use `/frag list` to see owned Fragments
- Use Fragment Changer to reactivate

---

## 📝 Version Info

**Current Version:** 1.0.0
**Last Updated:** December 2024
**Platform:** Paper/Spigot 1.20.4

---

*For detailed craftinsee CRAFTING_RECIPES.md*
*For full documentation, visit server wiki*
