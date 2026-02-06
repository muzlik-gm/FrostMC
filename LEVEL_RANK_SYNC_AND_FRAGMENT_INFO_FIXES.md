# Level-Rank Synchronization and Fragment Info Fixes

## Issue Summary
The level-rank synchronization system was not working bidirectionally because the formulas were using incorrect base values. Each fragment has different max levels and base ranks, but the system was using hardcoded formulas that assumed all fragments start at rank 1.

## Root Cause
The synchronization formulas in `LevelManager.java` were using:
- `rank = 1 + ((level - 1) * (maxRank - 1)) / (maxLevel - 1)` 
- `level = 1 + ((rank - 1) * (maxLevel - 1)) / (maxRank - 1)`

This assumed all fragments start at rank 1, but fragments actually start at their **base rank**.

## Fragment Base Ranks and Max Levels
Based on `FragmentRegistry.java`:

| Fragment | Base Rank | Max Rank | Max Level |
|----------|-----------|----------|-----------|
| FIRE     | 3         | 5        | 5         |
| WATER    | 2         | 4        | 5         |
| AIR      | 3         | 5        | 5         |
| DARK     | 4         | 6        | 8         |
| LIGHT    | 4         | 6        | 8         |
| VOID     | 7         | 9        | 10        |
| DRAGON   | 8         | 10       | 12        |
| STORM    | 6         | 8        | 10        |
| TIME     | 7         | 9        | 10        |
| LUCK     | 6         | 8        | 10        |

## Fixed Formulas

### Level → Rank Calculation
```java
private int calculateExpectedRank(int level, FragmentType type) {
    int maxLevel = getMaxLevel(type);
    int baseRank = rankManager.getBaseRank(type);
    int maxRank = rankManager.getMaxRank(type);
    
    // Formula: baseRank + floor((level - 1) * (maxRank - baseRank) / (maxLevel - 1))
    if (maxLevel <= 1) return baseRank;
    
    int rank = baseRank + ((level - 1) * (maxRank - baseRank)) / (maxLevel - 1);
    return Math.min(rank, maxRank);
}
```

### Rank → Level Calculation
```java
private int calculateExpectedLevel(int rank, FragmentType type) {
    int maxLevel = getMaxLevel(type);
    int baseRank = rankManager.getBaseRank(type);
    int maxRank = rankManager.getMaxRank(type);
    
    // Formula: 1 + floor((rank - baseRank) * (maxLevel - 1) / (maxRank - baseRank))
    if (maxRank <= baseRank) return 1;
    
    int level = 1 + ((rank - baseRank) * (maxLevel - 1)) / (maxRank - baseRank);
    return Math.min(level, maxLevel);
}
```

## Example Calculations

### Fire Fragment (baseRank=3, maxRank=5, maxLevel=5)
- Level 1 → Rank 3 (base rank)
- Level 3 → Rank 4 (middle)
- Level 5 → Rank 5 (max rank)

### Dragon Fragment (baseRank=8, maxRank=10, maxLevel=12)
- Level 1 → Rank 8 (base rank)
- Level 6 → Rank 9 (middle)
- Level 12 → Rank 10 (max rank)

## Testing Commands
```
/fragment set <player> level fire 5    # Should sync to rank 5
/fragment set <player> rank fire 3     # Should sync to level 1
/fragment set <player> level dragon 12 # Should sync to rank 10
/fragment set <player> rank dragon 8   # Should sync to level 1
```

## Files Modified
- `src/main/java/com/muzlik/fragment/level/LevelManager.java`
  - Fixed `calculateExpectedRank()` method
  - Fixed `calculateExpectedLevel()` method
  - Both methods now use fragment-specific base ranks instead of assuming rank 1

## Build Status
✅ **BUILD SUCCESSFUL** - Plugin compiled without errors

## Next Steps
1. Deploy the updated JAR file to the server
2. Test level-rank synchronization with various fragment types
3. Verify bidirectional sync works correctly (level→rank and rank→level)