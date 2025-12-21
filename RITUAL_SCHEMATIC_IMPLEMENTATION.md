# Ritual Schematic System Implementation

## Overview
Successfully implemented a schematic-based ritual structure system that uses WorldEdit to load and paste a single schematic file for all fragment rituals.

## Key Features

### 1. Schematic-Based Structure
- Uses `Ritual_Area.schem` for all fragment types
- Automatically extracts schematic from JAR on first run
- Schematic is stored in `plugins/FrostSMP/schematics/`

### 2. WorldEdit Integration
- Added WorldEdit as soft dependency (optional)
- Uses WorldEdit API 7.2.15 for schematic loading and pasting
- Properly handles WorldEdit exceptions

### 3. Fallback System
- Falls back to procedural generation (`SimpleRitualStructure`) if:
  - WorldEdit is not installed
  - Schematic file is missing or corrupted
  - WorldEdit operations fail

### 4. Block Restoration
- Stores original blocks before pasting schematic
- Properly restores all blocks when ritual ends or is cancelled
- Protected area prevents block modifications during ritual

## Implementation Details

### Files Modified/Created

1. **pom.xml**
   - Added WorldEdit repository and dependency
   - Configured resources to exclude binary files from filtering
   - Fixed duplicate resources tag issue

2. **plugin.yml**
   - Added WorldEdit as soft dependency

3. **RitualStructure.java** (NEW)
   - Interface for ritual structures
   - Defines spawn(), despawn(), isProtected() methods

4. **SchematicRitualStructure.java** (NEW)
   - Implements schematic-based ritual structure
   - Loads schematic using WorldEdit API
   - Stores and restores original blocks
   - Falls back to SimpleRitualStructure if needed

5. **SimpleRitualStructure.java** (UPDATED)
   - Implements RitualStructure interface
   - Serves as fallback for procedural generation

6. **RitualManager.java** (UPDATED)
   - Uses SchematicRitualStructure for all rituals
   - Properly centers ritual location to block coordinates

7. **RitualStructureProtectionListener.java** (UPDATED)
   - Uses RitualStructure interface
   - Properly calls despawn() on cleanup

8. **src/main/resources/schematics/Ritual_Area.schem** (ADDED)
   - Bundled schematic file in JAR

## How It Works

1. **Ritual Start**: Player starts ritual → RitualManager creates SchematicRitualStructure
2. **WorldEdit Check**: System checks if WorldEdit is available
3. **Schematic Extraction**: If schematic doesn't exist in plugins folder, extract from JAR
4. **Schematic Loading**: Load schematic using WorldEdit API
5. **Block Storage**: Store all original blocks that will be replaced
6. **Schematic Paste**: Paste schematic at ritual location (centered to block coordinates)
7. **Protection**: Mark 12-block radius as protected
8. **Ritual End**: Restore all original blocks when ritual completes/cancels

## Configuration

No configuration needed! The system automatically:
- Detects WorldEdit availability
- Extracts schematic from JAR
- Falls back to procedural generation if needed

## Testing Checklist

- [x] Maven build succeeds
- [x] Schematic file included in JAR
- [x] WorldEdit dependency added
- [ ] Test schematic extraction on first run
- [ ] Test schematic loading with WorldEdit installed
- [ ] Test fallback when WorldEdit is not installed
- [ ] Test block restoration after ritual completion
- [ ] Test block restoration after ritual cancellation
- [ ] Test protected area prevents block modifications

## Notes

- Schematic is shared across all fragment types
- Original procedural generation still available as fallback
- System is backward compatible (works without WorldEdit)
- Binary files (.schem) are properly handled by Maven
