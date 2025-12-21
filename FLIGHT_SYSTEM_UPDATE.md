# Flight System & Fire Ability Update

## Summary
Successfully updated the flight system with time limits and cooldowns, and changed the fire ability from pulling enemies to an expanding ring of fire.

## Flight System Changes

### Time Limits
- **Air Fragment Flight**: 1 minute (60 seconds)
- **Dragon Fragment Flight**: 10 minutes (600 seconds)

### Cooldown System
- **Cooldown Duration**: 1.5 minutes (90 seconds) after flight time expires
- Players are notified at 60s, 30s, and 10s remaining during cooldown
- "Flight ready!" message when cooldown completes

### Flight Notifications
- Shows remaining time at 30s, 15s, 10s, and 5s
- Plays sound effects at key intervals
- Clear activation message shows duration in seconds

### Removed Features
- Removed manual flight cancellation (sneak + left click hold)
- Removed recharge system (was for cancelled flights)
- Simplified to: Use flight → Time expires → Cooldown → Ready again

## Fire Ability Change

### Old Ability: Inferno Maelstrom (Pull)
- Created a vortex that pulled enemies toward the center
- Could be frustrating in PvP situations

### New Ability: Inferno Maelstrom (Expanding Ring)
- Creates an expanding ring of fire from the player
- Ring expands outward from radius 1.0 to max radius (scaled by rank)
- Enemies hit by the ring are:
  - Knocked back away from center
  - Damaged (scaled by rank)
  - Set on fire for 3 seconds
- Places temporary fire blocks along the ring path
- More visually impressive with expanding effect
- Better for crowd control without the frustration of being pulled

## Technical Details

### FlightManager Updates
- Replaced `RechargeSession` with `CooldownSession`
- Updated duration constants:
  ```java
  AIR_FLIGHT_DURATION = 60 seconds
  DRAGON_FLIGHT_DURATION = 600 seconds
  FLIGHT_COOLDOWN = 90 seconds
  ```
- Simplified session management (no more cancel attempts)

### InfernoMaelstromExecutor Updates
- Changed from pull mechanic to expanding ring
- Ring expands at 0.15 blocks per tick
- Knockback vector pushes enemies away from center
- Fire blocks placed along ring path (60 tick duration)
- Damage applied when entities are within 1.5 blocks of ring edge

### FlightControlListener Updates
- Simplified to only handle player quit events
- Removed sneak + left click cancel functionality

## Testing Checklist
- [ ] Air flight lasts exactly 1 minute
- [ ] Dragon flight lasts exactly 10 minutes
- [ ] Cooldown is 1.5 minutes after flight expires
- [ ] Cooldown notifications appear at correct intervals
- [ ] Flight cannot be activated during cooldown
- [ ] Inferno Maelstrom creates expanding ring
- [ ] Enemies are knocked back (not pulled in)
- [ ] Fire blocks spawn along ring path
- [ ] Damage and fire effect applied correctly

## Files Modified
1. `src/main/java/com/muzlik/fragment/ability/FlightManager.java`
2. `src/main/java/com/muzlik/fragment/ability/executors/fire/InfernoMaelstromExecutor.java`
3. `src/main/java/com/muzlik/listener/FlightControlListener.java`
