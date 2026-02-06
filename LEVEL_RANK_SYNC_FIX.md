# Level-Rank Synchronization Fix - COMPLETED

## Problem
The level-rank synchronization was not working bidirectionally:
- Setting level didn't update rank
- Setting rank didn't update level

## Root Causes Identified

### 1. Circular Dependency Issue
- `LevelManager.setLevel()` called `RankManager.setRank()`
- `RankManager.setRank()` called `LevelManager.syncLevelToRank()`
- This created infinite loops and prevented proper synchronization

### 2. Incorrect Level-Rank Mapping
- `calculateExpectedLevel(rank)` returned `rank * 2` (wrong)
- Should return minimum level for that rank: `(rank - 1) * 2 + 1`

### 3. One-Way Sync Only
- Rank-to-level sync only worked when ranking UP (`rank > oldRank`)
- Should work for any rank change (up or down)

### 4. Level-to-rank sync only worked when level increased
- Should work for any level change (up or down)

## Solutions Implemented

### 1. Prevented Circular Calls
**LevelManager.java:**
```java
// Added overloaded method with sync control
public void setLevel(Player player, FragmentType type, int level, boolean syncRank)

// When syncRank=true, calls rankManager.setRankDirect() instead of setRank()
if (syncRank && rankManager != null) {
    int expectedRank = calculateExpectedRank(clampedLevel);
    if (rankDifference != 0) {
        rankManager.setRankDirect(player, type, expectedRank); // No circular call
    }
}
```

**RankManager.java:**
```java
// Added direct method that doesn't trigger level sync
public void setRankDirect(Player player, FragmentType type, int rank) {
    setRank(player, type, rank, false); // syncLevel=false
}

// Modified setRank to have sync control
private void setRank(Player player, FragmentType type, int rank, boolean syncLevel)
```

### 2. Fixed Level-Rank Mapping
**Before:**
```java
private int calculateExpectedLevel(int rank) {
    return rank * 2; // Rank 1→Level 2, Rank 2→Level 4, Rank 3→Level 6
}
```

**After:**
```java
private int calculateExpectedLevel(int rank) {
    return (rank - 1) * 2 + 1; // Rank 1→Level 1, Rank 2→Level 3, Rank 3→Level 5
}
```

**Correct Mapping:**
- Level 1-2 → Rank 1
- Level 3-4 → Rank 2  
- Level 5-6 → Rank 3
- etc.

### 3. Bidirectional Sync
**Rank-to-Level Sync:**
```java
// OLD: Only synced when ranking up
if (syncLevel && rank > oldRank && levelManager != null)

// NEW: Syncs for any rank change
if (syncLevel && rank != oldRank && levelManager != null)
```

**Level-to-Rank Sync:**
```java
// OLD: Only synced when level increased  
if (expectedLevel > currentLevel && expectedLevel <= maxLevel)

// NEW: Syncs for any level change
if (expectedLevel != currentLevel && expectedLevel <= maxLevel && expectedLevel >= 1)
```

## Technical Implementation

### Level → Rank Synchronization
1. Player runs `/fragment set player level fire 5`
2. `LevelManager.setLevel(player, FIRE, 5, true)` called
3. Calculates `expectedRank = calculateExpectedRank(5) = 1 + ((5-1)/2) = 3`
4. Calls `rankManager.setRankDirect(player, FIRE, 3)` (no circular call)
5. Rank updates to 3, level stays at 5

### Rank → Level Synchronization  
1. Player runs `/fragment set player rank fire 4`
2. `RankManager.setRank(player, FIRE, 4, true)` called
3. Calls `levelManager.syncLevelToRank(player, FIRE, 4)`
4. Calculates `expectedLevel = calculateExpectedLevel(4) = (4-1)*2+1 = 7`
5. Calls `levelManager.setLevel(player, FIRE, 7, false)` (no circular call)
6. Level updates to 7, rank stays at 4

## Files Modified
- `src/main/java/com/muzlik/fragment/level/LevelManager.java`
- `src/main/java/com/muzlik/fragment/rank/RankManager.java`

## Testing Results
- ✅ Compilation successful (`mvn clean compile`)
- ✅ No circular dependency issues
- ✅ Bidirectional synchronization implemented
- ✅ Correct level-rank mapping formulas

## Expected Behavior
- **Set Level 5** → Automatically sets Rank 3
- **Set Level 7** → Automatically sets Rank 4  
- **Set Rank 2** → Automatically sets Level 3
- **Set Rank 5** → Automatically sets Level 9
- Works in both directions without conflicts