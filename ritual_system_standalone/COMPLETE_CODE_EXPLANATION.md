# Complete Ritual System Code Explanation

This document provides a comprehensive explanation of every component in the standalone ritual system, including the complete workflow, code architecture, and implementation details.

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Architecture & Data Flow](#architecture--data-flow)
3. [Core Components Explained](#core-components-explained)
4. [Visual Effects System](#visual-effects-system)
5. [Complete Workflow](#complete-workflow)
6. [Code Deep Dive](#code-deep-dive)
7. [Customization Guide](#customization-guide)
8. [Advanced Features](#advanced-features)

---

## System Overview

The ritual system is a **multi-component magical system** that creates immersive, time-based rituals in Minecraft. Players start rituals by using catalyst items, which spawn visual effects and require the player to stay nearby until completion.

### Key Concepts:
- **Rituals**: Time-based magical processes (3-8 minutes)
- **Catalysts**: Items consumed to start rituals
- **Crystals**: The magical focus of each ritual (Fire, Water, Air, etc.)
- **Proximity**: Players must stay within 5 blocks or ritual enters grace period
- **Grace Period**: 10-second window to return before ritual fails
- **Visual Display**: Floating crystals, ender crystal beams, particle effects

---

## Architecture & Data Flow

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Player Uses   │───▶│  RitualManager   │───▶│ RitualInstance  │
│ Catalyst Item   │    │ (Main Controller)│    │ (Data Storage)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │RitualDisplayMgr  │
                       │(Visual Effects)  │
                       └──────────────────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
            ┌─────────────┐ ┌─────────┐ ┌─────────────┐
            │Ender Crystal│ │Floating │ │Magic Circle │
            │   Beams     │ │ Crystal │ │ Particles   │
            └─────────────┘ └─────────┘ └─────────────┘
```

### Data Flow:
1. **Input**: Player right-clicks catalyst item
2. **Validation**: Check location, cooldowns, existing rituals
3. **Creation**: Spawn RitualInstance + visual display
4. **Update Loop**: Monitor progress, proximity, grace period
5. **Completion**: Execute completion logic + cleanup

---

## Core Components Explained

### 1. CrystalType.java - The Foundation

```java
public enum CrystalType {
    FIRE("Fire Crystal", "A blazing crystal of pure fire energy"),
    WATER("Water Crystal", "A flowing crystal of pure water energy"),
    // ... more types
}
```

**Purpose**: Defines the different types of magical crystals/items in your system.

**Key Features**:
- **Display Name**: Human-readable name shown to players
- **Description**: Lore text explaining the crystal's nature
- **Extensible**: Easy to add new crystal types

**Usage in System**:
- Determines visual colors (red for fire, blue for water)
- Affects completion rewards
- Influences particle effects and sounds

---

### 2. RitualType.java - Ritual Categories

```java
public enum RitualType {
    CRYSTAL_CREATION("Crystal Creation", 360),      // 6 minutes
    CRYSTAL_UPGRADE("Crystal Upgrade", 240),        // 4 minutes
    // ... more types
}
```

**Purpose**: Defines different categories of rituals with their durations.

**Key Features**:
- **Display Name**: What players see in messages
- **Duration**: How long the ritual takes (in seconds)
- **Extensible**: Add new ritual types easily

**Usage in System**:
- Determines ritual duration and boss bar display
- Controls completion logic (what happens when ritual finishes)
- Used in commands and item creation

---

### 3. RitualStage.java - Progress Tracking

```java
public enum RitualStage {
    CHARGING(0, 33),      // 0-33% progress
    ACTIVATION(33, 66),   // 33-66% progress
    COMPLETION(66, 100);  // 66-100% progress
}
```

**Purpose**: Tracks ritual progression through distinct stages.

**Key Features**:
- **Percentage Ranges**: Each stage covers a specific progress range
- **Visual Feedback**: Different effects can be shown per stage
- **Milestone System**: Allows stage-specific notifications

**Usage in System**:
- Updates automatically as ritual progresses
- Can trigger different visual/audio effects per stage
- Provides clear feedback to players about ritual status

---

### 4. RitualInstance.java - Individual Ritual Data

This is the **data container** for each active ritual. It stores all information about a specific ritual in progress.

#### Core Data:
```java
private final UUID playerId;           // Who started the ritual
private final RitualType type;         // What kind of ritual
private final Location location;       // Where the ritual is happening
private final long startTime;          // When it started (for progress calculation)
private final long duration;           // How long it should take
private final ItemStack catalyst;      // The item that was consumed
private final CrystalType crystalType; // What crystal type this is
```

#### Progress Tracking:
```java
public int getProgressPercent() {
    long elapsed = System.currentTimeMillis() - startTime;
    return (int) Math.min(100, (elapsed * 100) / duration);
}
```

#### Grace Period System:
```java
private boolean inGracePeriod = false;
private long gracePeriodStartTime = 0;
private static final long GRACE_PERIOD_DURATION = 10000; // 10 seconds

public void startGracePeriod() {
    if (!inGracePeriod) {
        inGracePeriod = true;
        gracePeriodStartTime = System.currentTimeMillis();
    }
}
```

**Grace Period Logic**:
- When no players are in ritual area, grace period starts
- Players have 10 seconds to return
- If they return, ritual resumes normally
- If time expires, ritual fails

---

### 5. RitualManager.java - The Main Controller

This is the **heart of the system**. It manages all active rituals and coordinates between components.

#### Key Responsibilities:
1. **Ritual Lifecycle**: Start, update, complete, fail rituals
2. **Validation**: Check locations, cooldowns, prerequisites
3. **Proximity Monitoring**: Track if players are near rituals
4. **Boss Bar Management**: Show progress to all players
5. **Cleanup**: Remove displays and restore blocks

#### Starting a Ritual:
```java
public boolean startRitual(Player player, RitualType type, ItemStack catalyst, CrystalType crystalType) {
    // 1. Validation checks
    if (hasActiveRitual(player)) {
        player.sendMessage("§c✗ You already have an active ritual");
        return false;
    }
    
    // 2. Location validation
    if (!isValidRitualLocation(player, ritualLocation)) {
        return false; // Must be in Overworld, above ground
    }
    
    // 3. Consume catalyst item
    player.getInventory().removeItem(catalyst);
    
    // 4. Create ritual instance
    RitualInstance ritual = new RitualInstance(/*...*/);
    activeRituals.put(player.getUniqueId(), ritual);
    
    // 5. Create visual display
    displayManager.createDisplay(player, location, crystalType, crystalItem, duration);
    
    // 6. Create boss bar
    createRitualBossBar(player, ritual);
    
    return true;
}
```

#### Update Loop (runs every second):
```java
private void updateRitual(UUID ritualOwnerId, RitualInstance ritual) {
    // 1. Check if any player is in ritual area
    boolean anyPlayerInArea = isAnyPlayerInRitualArea(ritual);
    
    // 2. Handle grace period logic
    if (!anyPlayerInArea) {
        if (!ritual.isInGracePeriod()) {
            ritual.startGracePeriod(); // Start 10-second countdown
        } else if (ritual.isGracePeriodExpired()) {
            failRitual(ritualOwnerId, ritual, "No players in area");
            return;
        }
    } else {
        ritual.endGracePeriod(); // Player returned, resume ritual
    }
    
    // 3. Update progress and check milestones
    int progressPercent = ritual.getProgressPercent();
    checkProgressMilestones(owner, ritual, previousPercent, progressPercent);
    
    // 4. Update boss bar
    updateRitualBossBar(ritualOwnerId, ritual);
    
    // 5. Check completion
    if (elapsed >= ritual.getDuration()) {
        completeRitual(ritualOwnerId, ritual);
    }
}
```

#### Completion System:
```java
private void completeRitual(UUID ownerId, RitualInstance ritual) {
    // Remove from active rituals
    activeRituals.remove(ownerId);
    removeRitualBossBar(ownerId);
    
    // Execute type-specific completion logic
    switch (ritual.getType()) {
        case CRYSTAL_CREATION:
            completeCrystalCreation(owner, crystalType);
            break;
        case CRYSTAL_UPGRADE:
            completeCrystalUpgrade(owner, crystalType);
            break;
        // ... more cases
    }
    
    // Cleanup visual display
    displayManager.removeDisplay(ownerId);
    
    // Broadcast completion
    Bukkit.broadcastMessage("§a§l✓ RITUAL COMPLETE! " + ownerName + " completed " + ritualType);
}
```

---

### 6. RitualDisplayManager.java - Visual Effects Engine

This creates the **spectacular visual display** that makes rituals impressive and immersive.

#### Display Components:
1. **Floating Crystal**: The central focus item that hovers in the air
2. **Ender Crystal Beams**: 6 ender crystals in a circle shooting beams at the floating crystal
3. **Timer Hologram**: Shows remaining time above the crystal
4. **Particle Effects**: Magic circles, spirals, and ambient particles
5. **Ambient Sounds**: Atmospheric audio to enhance immersion

#### Creating the Display:
```java
public void createDisplay(Player player, Location center, CrystalType crystalType, ItemStack crystalItem, long durationMs) {
    World world = center.getWorld();
    RitualDisplay display = new RitualDisplay();
    
    // 1. Create floating crystal item
    Location itemLoc = center.clone().add(0, CRYSTAL_HEIGHT, 0); // 3.5 blocks up
    Item floatingItem = world.dropItem(itemLoc, crystalItem);
    floatingItem.setPickupDelay(Integer.MAX_VALUE); // Can't be picked up
    floatingItem.setGravity(false); // Floats in place
    floatingItem.setGlowing(true); // Glowing effect
    floatingItem.setCustomName("§5§l" + crystalType.getDisplayName());
    
    // 2. Create ender crystal beam sources (6 crystals in a circle)
    for (int i = 0; i < BEAM_COUNT; i++) {
        double angle = (2 * Math.PI * i) / BEAM_COUNT;
        double x = center.getX() + BEAM_SOURCE_DISTANCE * Math.cos(angle); // 25 blocks away
        double z = center.getZ() + BEAM_SOURCE_DISTANCE * Math.sin(angle);
        Location beamSource = new Location(world, x, center.getY() + BEAM_SOURCE_HEIGHT, z); // 15 blocks up
        
        EnderCrystal crystal = (EnderCrystal) world.spawnEntity(beamSource, EntityType.ENDER_CRYSTAL);
        crystal.setBeamTarget(beamTargetLoc.toBlockLocation()); // Point at floating crystal
    }
    
    // 3. Create timer hologram
    ArmorStand timerStand = (ArmorStand) world.spawnEntity(timerLoc, EntityType.ARMOR_STAND);
    timerStand.setVisible(false); // Invisible armor stand
    timerStand.setCustomName("§7Ritual in progress...");
    
    // 4. Start update task for effects
    startDisplayUpdate(player.getUniqueId(), display);
}
```

#### Particle Effects System:
```java
private void addRitualParticleEffects(RitualDisplay display, int tick) {
    Location crystalLoc = display.center.clone().add(0, CRYSTAL_HEIGHT, 0);
    Color crystalColor = getCrystalColor(display.crystalType);
    
    // 1. Spiral particles rising around crystal
    double spiralAngle = tick * 0.2;
    double spiralRadius = 0.5;
    double spiralHeight = (tick % 40) * 0.1; // Rises 4 blocks then resets
    Location spiralLoc = crystalLoc.clone().add(
        spiralRadius * Math.cos(spiralAngle),
        spiralHeight - 2.0,
        spiralRadius * Math.sin(spiralAngle)
    );
    world.spawnParticle(Particle.REDSTONE, spiralLoc, 1, 0, 0, 0, 0, dustOptions);
    
    // 2. Pulsing glow around crystal
    if (tick % 10 == 0) {
        double pulseSize = 0.3 + 0.2 * Math.sin(tick * 0.1);
        world.spawnParticle(Particle.REDSTONE, crystalLoc, 8, pulseSize, pulseSize, pulseSize, 0, dustOptions);
    }
    
    // 3. Magic circle on ground
    drawMagicCircle(display, tick);
    
    // 4. Energy flow from circle to crystal
    // 5. Dramatic burst effects every 5 seconds
}
```

#### Magic Circle System:
```java
private void drawMagicCircle(RitualDisplay display, int tick) {
    Location center = display.center.clone();
    Color circleColor = getCrystalColor(display.crystalType);
    double rotation = tick * 0.02; // Slow rotation
    
    // Main circle (3 block radius)
    drawCircle(world, center, 3.0, dustOptions, rotation, 50);
    
    // Inner circle (2 block radius, counter-rotating)
    drawCircle(world, center, 2.0, dustOptions, -rotation * 1.5, 35);
    
    // Runes around outer edge
    drawRuneCircle(world, center, 3.5, dustOptions, rotation * 0.5, 8);
}

private void drawCircle(World world, Location center, double radius, Particle.DustOptions dust, double rotation, int points) {
    for (int i = 0; i < points; i++) {
        double angle = (2 * Math.PI * i / points) + rotation;
        double x = center.getX() + radius * Math.cos(angle);
        double z = center.getZ() + radius * Math.sin(angle);
        Location particleLoc = new Location(world, x, center.getY() + 1.1, z);
        world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0, dust);
    }
}
```

---

## Visual Effects System

### Ender Crystal Beam Configuration

The system uses **6 Ender Crystals** positioned in a perfect circle around the ritual site:

```
                    Crystal 1 (North)
                         │
                         │ Beam
                         ▼
Crystal 6 ◄─────── Floating Crystal ─────── ► Crystal 2
                         ▲
                         │ Beam  
                         │
                    Crystal 5 (South)
```

**Technical Details**:
- **Distance**: 25 blocks from center
- **Height**: 15 blocks above ground
- **Beam Target**: Invisible armor stand 1.6 blocks below floating crystal
- **Visual Effect**: Purple/colored energy beams converging on the crystal

### Particle Effects Layers

The system creates **5 layers** of particle effects:

1. **Spiral Particles**: Rise around the floating crystal in a helix pattern
2. **Pulsing Glow**: Expanding/contracting aura around the crystal
3. **Magic Circles**: Rotating particle circles on the ground (3 layers)
4. **Energy Flow**: Particles flowing from circle edge to crystal
5. **Burst Effects**: Dramatic explosions every 5 seconds

### Color System

Each crystal type has a unique color scheme:

```java
private Color getCrystalColor(CrystalType type) {
    return switch (type) {
        case FIRE -> Color.fromRGB(255, 69, 0);      // Orange Red
        case WATER -> Color.fromRGB(0, 191, 255);    // Deep Sky Blue
        case AIR -> Color.fromRGB(230, 230, 250);    // Lavender
        case EARTH -> Color.fromRGB(139, 69, 19);    // Saddle Brown
        case DARK -> Color.fromRGB(75, 0, 130);      // Indigo
        case LIGHT -> Color.fromRGB(255, 215, 0);    // Gold
        // ... more colors
    };
}
```

---

## Complete Workflow

### 1. Ritual Initiation

```
Player Right-Clicks Catalyst Item
           │
           ▼
    Validate Location
    (Overworld, Above Ground)
           │
           ▼
    Check Prerequisites
    (No Active Ritual, Not on Cooldown)
           │
           ▼
    Consume Catalyst Item
           │
           ▼
    Create RitualInstance
           │
           ▼
    Spawn Visual Display
    (Floating Crystal + Ender Crystal Beams)
           │
           ▼
    Create Boss Bar
    (Visible to All Players)
           │
           ▼
    Start Update Loop
```

### 2. Ritual Progress Loop (Every Second)

```
Check Player Proximity
    │
    ├─ No Players in Area
    │   │
    │   ├─ Grace Period Not Started
    │   │   └─ Start 10-Second Grace Period
    │   │
    │   └─ Grace Period Active
    │       │
    │       ├─ Time Remaining
    │       │   └─ Update Grace Period Boss Bar
    │       │
    │       └─ Time Expired
    │           └─ FAIL RITUAL
    │
    └─ Players in Area
        │
        ├─ Grace Period Active
        │   └─ End Grace Period, Resume Ritual
        │
        └─ Normal Progress
            │
            ├─ Update Progress Percentage
            │
            ├─ Check Milestones (25%, 50%, 75%, 90%)
            │   └─ Send Notifications + Play Sounds
            │
            ├─ Update Boss Bar
            │
            ├─ Update Visual Effects
            │
            └─ Check Completion
                │
                ├─ Not Complete: Continue Loop
                │
                └─ Complete: Execute Completion Logic
```

### 3. Ritual Completion

```
Remove from Active Rituals
           │
           ▼
Execute Type-Specific Logic
    │
    ├─ CRYSTAL_CREATION
    │   └─ Give Player Crystal Item
    │
    ├─ CRYSTAL_UPGRADE  
    │   └─ Upgrade Player's Crystal
    │
    ├─ POWER_INFUSION
    │   └─ Grant Player Powers
    │
    └─ ENCHANTMENT_RITUAL
        └─ Enchant Player's Items
           │
           ▼
Remove Visual Display
(Floating Crystal, Ender Crystals, Particles)
           │
           ▼
Remove Boss Bar
           │
           ▼
Broadcast Completion Message
           │
           ▼
Play Completion Effects
```

### 4. Ritual Failure

```
Remove from Active Rituals
           │
           ▼
Remove Visual Display
           │
           ▼
Remove Boss Bar
           │
           ▼
Apply Failure Cooldown
(1 minute before next ritual)
           │
           ▼
Send Failure Message to Player
           │
           ▼
Play Failure Effects
```

---

## Code Deep Dive

### Boss Bar System

The boss bar provides **real-time progress feedback** to all players on the server:

```java
private void createRitualBossBar(Player player, RitualInstance ritual) {
    CrystalType crystalType = ritual.getCrystalType();
    BarColor color = getCrystalBarColor(crystalType); // Fire = RED, Water = BLUE, etc.
    Location loc = ritual.getLocation();
    
    // Create descriptive title with location
    String title = String.format("§e⚡ %s §8- §b%s §8| §7Location: §f%d, %d, %d", 
        ritual.getType().getDisplayName(),
        crystalType.getDisplayName(),
        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    
    BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SEGMENTED_10);
    bossBar.setProgress(0.0);
    
    // Add ALL online players (not just ritual owner)
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        bossBar.addPlayer(onlinePlayer);
    }
    
    ritualBossBars.put(player.getUniqueId(), bossBar);
}
```

**Boss Bar Updates**:
- **Normal Progress**: Shows percentage and time remaining
- **Grace Period**: Changes to red and shows countdown
- **Completion**: Removed automatically

### Proximity Detection System

The system monitors if **any player** (not just the owner) is within the ritual area:

```java
private boolean isAnyPlayerInRitualArea(RitualInstance ritual) {
    Location ritualLoc = ritual.getLocation();
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getWorld().equals(ritualLoc.getWorld()) && 
            player.getLocation().distance(ritualLoc) <= proximityDistance) { // 5 blocks
            return true;
        }
    }
    return false;
}
```

**Key Features**:
- **Any Player**: Not just the ritual owner can maintain it
- **World Check**: Must be in same world
- **Distance Check**: Within 5 blocks (configurable)
- **Grace Period**: 10-second window if no players present

### Milestone Notification System

Provides feedback at key progress points:

```java
private void checkProgressMilestones(Player owner, RitualInstance ritual, int previousPercent, int currentPercent) {
    int[] milestones = {25, 50, 75, 90};
    
    for (int milestone : milestones) {
        if (previousPercent < milestone && currentPercent >= milestone) {
            String message = switch (milestone) {
                case 25 -> "§e⚡ Ritual Progress: §b25% §7- Quarter complete";
                case 50 -> "§e⚡ Ritual Progress: §b50% §7- Halfway there!";
                case 75 -> "§e⚡ Ritual Progress: §b75% §7- Almost done!";
                case 90 -> "§e⚡ Ritual Progress: §b90% §7- Final stage!";
                default -> "";
            };
            owner.sendMessage(message);
            owner.playSound(owner.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        }
    }
}
```

### Location Validation

Ensures rituals can only be performed in appropriate locations:

```java
private boolean isValidRitualLocation(Player player, Location location) {
    // Must be in Overworld
    if (location.getWorld().getEnvironment() != World.Environment.NORMAL) {
        player.sendMessage("§c✗ Rituals can only be performed in the Overworld!");
        return false;
    }
    
    // Must be above ground (within 5 blocks of surface)
    Block highestBlock = location.getWorld().getHighestBlockAt(location);
    int surfaceY = highestBlock.getY();
    int playerY = location.getBlockY();
    
    if (playerY < surfaceY - 5) {
        player.sendMessage("§c✗ Rituals must be performed above ground!");
        player.sendMessage("§7You are underground. Find an open area under the sky.");
        return false;
    }
    
    return true;
}
```

---

## Customization Guide

### Adding New Crystal Types

1. **Add to CrystalType enum**:
```java
SHADOW("Shadow Crystal", "A mysterious crystal of shadow energy"),
```

2. **Add color mapping**:
```java
case SHADOW -> Color.fromRGB(64, 64, 64); // Dark gray
```

3. **Add boss bar color**:
```java
case SHADOW -> BarColor.PURPLE;
```

4. **Add completion logic**:
```java
case SHADOW -> completeShadowRitual(player);
```

### Adding New Ritual Types

1. **Add to RitualType enum**:
```java
SOUL_BINDING("Soul Binding", 480), // 8 minutes
```

2. **Add completion handler**:
```java
case SOUL_BINDING:
    completeSoulBinding(owner, crystalType);
    break;
```

3. **Implement completion method**:
```java
private void completeSoulBinding(Player player, CrystalType crystalType) {
    player.sendMessage("§a✓ Soul Binding complete!");
    // Your custom logic here
}
```

### Modifying Visual Effects

**Change particle colors**:
```java
private Color getCrystalColor(CrystalType type) {
    // Add your custom colors
    return switch (type) {
        case CUSTOM_TYPE -> Color.fromRGB(255, 0, 255); // Magenta
        default -> Color.fromRGB(128, 128, 128); // Default gray
    };
}
```

**Add new particle effects**:
```java
// In addRitualParticleEffects method
if (tick % 15 == 0) {
    // Your custom particle effect
    world.spawnParticle(Particle.DRAGON_BREATH, crystalLoc, 10, 0.5, 0.5, 0.5, 0.1);
}
```

**Modify magic circle complexity**:
```java
// Add more circles
drawCircle(world, center, 4.0, dustOptions, rotation * 0.7, 60); // Outer circle
drawCircle(world, center, 1.0, dustOptions, -rotation * 2.0, 20); // Inner circle
```

### Changing Durations and Distances

**Ritual durations** (in RitualType.java):
```java
CRYSTAL_CREATION("Crystal Creation", 600), // Change to 10 minutes
```

**Proximity distance** (in RitualManager.java):
```java
private double proximityDistance = 8.0; // Change to 8 blocks
```

**Grace period duration** (in RitualInstance.java):
```java
private static final long GRACE_PERIOD_DURATION = 15000; // Change to 15 seconds
```

**Visual effect distances** (in RitualDisplayManager.java):
```java
private static final double BEAM_SOURCE_DISTANCE = 30.0; // Crystals further away
private static final double CRYSTAL_HEIGHT = 5.0; // Floating crystal higher
```

---

## Advanced Features

### Thread Safety

The system uses **ConcurrentHashMap** for thread-safe operations:
```java
private final Map<UUID, RitualInstance> activeRituals = new ConcurrentHashMap<>();
```

This prevents issues when multiple threads access ritual data simultaneously.

### Memory Management

**Automatic Cleanup**:
- Entities are removed when rituals end
- Boss bars are properly disposed
- Update tasks are cancelled
- Maps are cleared on shutdown

**Entity Persistence**:
```java
floatingItem.setPersistent(true); // Survives chunk unloads
floatingItem.setUnlimitedLifetime(true); // Never despawns naturally
```

### Performance Optimization

**Reduced Update Frequency**:
```java
// Timer updates only every 20 ticks (1 second)
if (tick % 20 == 0) {
    updateTimer();
}

// Position updates only every 40 ticks (2 seconds)
if (tick % 40 == 0) {
    updateFloatingItemPosition();
}
```

**Particle Throttling**:
- Magic circles update every 5 ticks instead of every tick
- Burst effects only every 100 ticks (5 seconds)
- Ambient sounds spaced out to prevent spam

### Error Handling

**Graceful Degradation**:
```java
try {
    Block block = entry.getKey().getBlock();
    block.setBlockData(entry.getValue(), false);
} catch (Exception e) {
    // Ignore errors during cleanup - don't crash the plugin
}
```

**Null Safety**:
```java
if (display.floatingItem != null && !display.floatingItem.isDead()) {
    display.floatingItem.remove();
}
```

### Integration Points

**Event System**: Easy to add custom events
```java
// Call custom event when ritual completes
RitualCompleteEvent event = new RitualCompleteEvent(player, ritualType, crystalType);
Bukkit.getPluginManager().callEvent(event);
```

**Permission System**: Add permission checks
```java
if (!player.hasPermission("ritual.use." + ritualType.name().toLowerCase())) {
    player.sendMessage("§c✗ You don't have permission for this ritual type!");
    return false;
}
```

**Economy Integration**: Add costs
```java
double cost = getRitualCost(ritualType, crystalType);
if (!economy.withdrawPlayer(player, cost).transactionSuccess()) {
    player.sendMessage("§c✗ You need $" + cost + " to perform this ritual!");
    return false;
}
```

---

## Summary

This ritual system provides a **complete, immersive magical experience** with:

✅ **Visual Spectacle**: Ender crystal beams, floating items, particle effects
✅ **Player Engagement**: Proximity requirements, progress feedback, milestone notifications  
✅ **Flexible Design**: Easy to add new crystal types, ritual types, and effects
✅ **Robust Logic**: Grace periods, validation, error handling, cleanup
✅ **Performance Optimized**: Throttled updates, memory management, thread safety
✅ **Integration Ready**: Events, permissions, economy hooks available

The system is designed to be **both impressive to players and easy for developers** to understand, modify, and extend. Each component has a clear purpose and the overall architecture is modular and maintainable.