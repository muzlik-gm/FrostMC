# FrostSMP Plugin

A comprehensive Minecraft plugin featuring a Fragment-based power system with 10 unique fragments, each with custom abilities, VFX, and progression mechanics.

## Features

### Fragment System
- **10 Unique Fragments:** Fire, Water, Air, Earth, Dark, Light, Void, Storm, Dragon, Mob
- **Progression System:** Level up (1-50) and rank up (1-8) each fragment
- **40+ Custom Abilities:** Each fragment has 4-5 unique abilities with VFX
- **Mana System:** Resource management with regeneration and flask crafting

### Advanced Mechanics
- **Flight System:** Duration-limited flight with visual carpet effects and recharge mechanics
- **Passive Abilities:** Phoenix Rebirth auto-revival and other triggered abilities
- **Boss XP Rewards:** Special XP for defeating major bosses
- **Ritual System:** Fragment creation through multi-block rituals
- **Custom Crafting:** Mana flasks and fragment-related items

### Visual Effects
- **5-Layer VFX System:** Core, Secondary, Ambient, Impact, and Cinematic layers
- **Rank-Based Scaling:** Particle effects scale with fragment rank
- **Performance Optimized:** Adaptive particle density based on server load
- **Cinematic Effects:** Slow-motion, screen shake, wind distortion, and more

### UI/UX
- **Clean GUI:** Small caps unicode styling with fragment overview
- **Action Bar HUD:** Real-time mana, XP, and ability cooldowns
- **Fragment Icons:** Custom textures for all abilities
- **Progress Tracking:** Visual progress bars and notifications

## Installation

1. Download the latest `FrostSMP.jar` from releases
2. Place in your server's `plugins` folder
3. Restart the server
4. (Optional) Install the resource pack for custom textures

## Building from Source

### Requirements
- Java 21+
- Maven 3.6+

### Build Commands
```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Deploy to server (Windows)
deploy.bat
```

## Configuration

See `SYSTEM.md` for detailed system documentation and `CRAFTING_RECIPES.md` for all crafting recipes.

## Commands

- `/fragment gui` - Open fragment overview GUI
- `/fragment give <player> <fragment>` - Give a fragment to a player
- `/fragment set <player> <fragment> <level/rank> <value>` - Set fragment stats
- `/fragment reset <player> [fragment]` - Reset fragment progress
- `/fragment reload` - Reload configuration

## Permissions

- `frostsmp.fragment.admin` - Access to admin commands
- `frostsmp.fragment.use` - Use fragment abilities (default: true)

## Technical Details

### Architecture
- **Modular Design:** Separate managers for fragments, mana, levels, ranks, abilities
- **Event-Driven:** Listeners for abilities, passives, flight control, data persistence
- **VFX Engine:** Dedicated system for particle effects and sound management
- **Data Persistence:** JSON-based player data storage

### Performance
- Adaptive particle density based on TPS
- Efficient cooldown management
- Optimized entity targeting
- Cleanup tasks for expired effects

## Credits

Developed for FrostSMP server.

## License

All rights reserved.
