# Fragment Info Display Improvements

## Problem
The current `/fragment info` command output is ugly and poorly formatted with basic text and inconsistent styling.

## Solution
Complete redesign with modern box-drawing characters, beautiful progress bars, proper spacing, and consistent color schemes.

## New Features

### 1. Beautiful Box Design
- Uses Unicode box-drawing characters (╔═╗║╚═╝)
- Proper padding and alignment
- Clean sectioned layout

### 2. Modern Progress Bars
- Uses █ (filled) and ░ (empty) characters
- Custom colors for different stats
- Better visual representation

### 3. Fragment-Specific Colors
- Each fragment has its own color theme
- Fire: Red, Water: Aqua, Air: White, etc.
- Consistent throughout the display

### 4. Enhanced Information Display
- Rank badges with symbols (★ ◆)
- Status indicators (✓ ✗ ⏱)
- Helpful tips and navigation hints

### 5. Improved Abilities Section
- Clear unlock status
- Cooldown indicators
- Slot information
- Progress tracking

## Implementation
The new design uses the same `sendStyledMessage` pattern as the tutorial system for consistent formatting across the plugin.

## Before vs After

### Before (Ugly)
```
§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬
§e§l              FIRE FRAGMENT
§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬

§7Rank: §6—† §6§l3 §7/ §65
§7  §8†' §7Next rank unlocks more power!

§7Level: §e§l15 §7/ §e50
§7XP: §7[§a–ˆ–ˆ–ˆ–ˆ–ˆ–ˆ§7–'–'–'–'–'–'–'–'–'–'–'–'–'–'§7]
```

### After (Beautiful)
```
╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║  🔥 ✦ FIRE FRAGMENT ✦                                        ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Rank: ◆ 3 / 5                                               ║
║  Next rank unlocks more power                                ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Level 15 / 50                                               ║
║  Experience: [██████████░░░░░░░░░░░░░░░░░░░░]                ║
║  1250 / 2500 (50.0%)                                        ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Mana: 150 / 200                                             ║
║  [███████████████████████░░░░░░░]                            ║
║  Regeneration: +2.5 per second                              ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  ABILITIES                                                   ║
║                                                              ║
║  ✓ Flame Burst [Slot 0]                                     ║
║  ✓ Fire Dome [Slot 1]                                       ║
║  ✗ Phoenix Rebirth [Rank 4 Required]                        ║
║                                                              ║
║  Progress: 2 / 5 abilities unlocked                         ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  💡 Use /fragment abilities for detailed ability info        ║
║  ⚡ Use /fragment level for character progression            ║
║  🎮 Use /fragment controls for control settings             ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

## Benefits
1. **Professional Appearance**: Looks like a modern game interface
2. **Better UX**: Clear sections and visual hierarchy
3. **Consistent Styling**: Matches tutorial system formatting
4. **More Information**: Helpful tips and navigation hints
5. **Fragment Identity**: Each fragment feels unique with its colors