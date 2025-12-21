# Ritual System Improvements

## Summary
Implemented major improvements to the ritual system including validation, delayed cleanup, and visual enhancements.

## ✅ Implemented Features

### 1. **Ritual Location Validation**
- **Overworld Only**: Rituals can only be performed in the Overworld (not Nether or End)
- **Above Ground Check**: Scans 150 blocks upward to ensure no blocks above
- **Clear Error Messages**: Players get specific feedback about why ritual failed
- **Example Messages**:
  - "§c✗ Rituals can only be performed in the Overworld!"
  - "§c✗ Rituals must be performed above ground!"
  - Shows exact block type and Y-level that's blocking

### 2. **Delayed Structure Cleanup (20 seconds)**
- **Countdown Timer**: Hologram displays remaining time above ritual center
- **Color-Coded**: 
  - Yellow (20-11s): "§e⏳ Cleanup in Xs"
  - Orange (10-6s): "§6⏳ Cleanup in Xs"
  - Red (5-1s): "§c⏳ Cleanup in Xs"
- **Smooth Cleanup**: Structure remains for 20 seconds after ritual completes
- **Automatic Removal**: Countdown hologram and structure removed together

### 3. **Side Circles Positioning**
- **Increased Distance**: Moved from 35 to 36 blocks from center
- **Better Visibility**: Ender crystal beams are more spread out
- **Improved Aesthetics**: More dramatic circular formation

### 4. **Schematic Positioning Fix**
- **User Adjustment**: Changed to Y-1 as requested
- **Proper Centering**: Schematic centers correctly at ritual location
- **Block Coordinate Alignment**: Everything aligns to block grid

## Technical Implementation

### RitualManager.java
```java
private boolean isValidRitualLocation(Player player, Location location) {
    // Check Overworld only
    if (location.getWorld().getEnvironment() != World.Environment.NORMAL) {
        return false;
    }
    
    // Check 150 blocks above for any solid blocks
    for (int y = location.getBlockY() + 1; y < maxHeight; y++) {
        if (block.getType().isSolid() && !block.getType().isAir()) {
            return false; // Cave or underground
        }
    }
    
    return true;
}
```

### RitualStructureProtectionListener.java
```java
private void scheduleDelayedDespawn(RitualStructure structure) {
    // Create countdown hologram
    ArmorStand countdown = spawnCountdownHologram();
    
    // 20-second countdown task
    new BukkitRunnable() {
        int secondsLeft = 20;
        
        @Override
        public void run() {
            if (secondsLeft <= 0) {
                countdown.remove();
                structure.despawn();
                cancel();
                return;
            }
            
            // Update countdown display with color coding
            String color = secondsLeft <= 5 ? "§c" : secondsLeft <= 10 ? "§6" : "§e";
            countdown.setCustomName(color + "⏳ Cleanup in " + secondsLeft + "s");
            
            secondsLeft--;
        }
    }.runTaskTimer(plugin, 0L, 20L);
}
```

### RitualDisplayManager.java
```java
// Increased beam source distance for better spread
private static final double BEAM_SOURCE_DISTANCE = 36.0; // Was 35.0
```

## User Experience Improvements

### Before
- Could perform rituals in caves/underground
- Could perform rituals in Nether/End
- Structure disappeared immediately after ritual
- Side circles too close together
- No feedback on why ritual failed

### After
- ✅ Must be above ground with clear sky
- ✅ Overworld only (realistic ritual requirements)
- ✅ 20-second grace period to admire completed ritual
- ✅ Countdown timer shows when cleanup happens
- ✅ Better visual spacing of ender crystal beams
- ✅ Clear, specific error messages

## Additional Quality of Life Features

### Validation Messages
- Tells players exactly what's wrong
- Shows block type and Y-level blocking ritual
- Suggests solutions ("Find an open area under the sky")

### Visual Polish
- Color-coded countdown (yellow → orange → red)
- Hologram positioned above ritual center
- Smooth transition from ritual to cleanup

### Safety Features
- Prevents accidental underground rituals
- Ensures rituals are visible and dramatic
- Protects against griefing in wrong dimensions

## Testing Checklist
- [ ] Ritual blocked in Nether
- [ ] Ritual blocked in End
- [ ] Ritual blocked in caves (blocks above)
- [ ] Ritual works in open plains
- [ ] Countdown timer appears after ritual
- [ ] Countdown changes color at 10s and 5s
- [ ] Structure despawns after 20 seconds
- [ ] Hologram removed with structure
- [ ] Side circles are further apart
- [ ] Error messages are clear and helpful

## Files Modified
1. `src/main/java/com/muzlik/ritual/RitualManager.java` - Added validation
2. `src/main/java/com/muzlik/ritual/structure/RitualStructureProtectionListener.java` - Added delayed cleanup
3. `src/main/java/com/muzlik/ritual/RitualDisplayManager.java` - Moved side circles
4. `src/main/java/com/muzlik/ritual/structure/SchematicRitualStructure.java` - Y-1 adjustment

## Future Enhancement Ideas
- Add weather requirements (clear sky bonus?)
- Add time of day requirements (midnight rituals?)
- Add particle effects during countdown
- Add sound effects for countdown milestones
- Add ritual success rate based on location quality
