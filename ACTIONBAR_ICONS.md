# Action Bar Fragment Icons

## Overview

The action bar now displays fragment icons using Unicode private use area characters that are mapped to custom textures via the resource pack.

## How It Works

### Unicode Mapping
- Fragment icons use Unicode characters in the private use area (U+E000 to U+F8FF)
- Each fragment's custom model data (1000-1099) is mapped to a Unicode character
- Example: Fire Fragment (CMD 1000) → \uE000, Water Fragment (CMD 1001) → \uE001

### Resource Pack Configuration

The resource pack must define font mappings in `assets/minecraft/font/default.json`:

```json
{
  "providers": [
    {
      "type": "bitmap",
      "file": "minecraft:item/fire.png",
      "ascent": 8,
      "height": 8,
      "chars": ["\uE000"]
    },
    {
      "type": "bitmap",
      "file": "minecraft:item/water.png",
      "ascent": 8,
      "height": 8,
      "chars": ["\uE001"]
    }
    // ... repeat for all fragments
  ]
}
```

### Action Bar Format

**With Mana System Enabled:**
```
[Icon] ✦ LVL: X/MAX │ Fragment ⚡ Mana/Max [■ ■ ○ ✗ ✗]
```

**With Mana System Disabled:**
```
[Icon] Fragment [■ ■ ○ ✗ ✗]
```

### Changes Made

1. **Fragment Icon Display**: Added `getFragmentIcon()` method that converts fragment custom model data to Unicode characters
2. **Character Level Toggle**: Character level now only shows when mana system is enabled (since character level affects max mana)
3. **Import Added**: Added `TextureRegistry` import to access fragment texture mappings

## Fragment Icon Mappings

| Fragment | Custom Model Data | Unicode Character |
|----------|-------------------|-------------------|
| Fire     | 1000              | \uE000            |
| Water    | 1001              | \uE001            |
| Air      | 1002              | \uE002            |
| Dark     | 1003              | \uE003            |
| Light    | 1004              | \uE004            |
| Void     | 1005              | \uE005            |
| Dragon   | 1006              | \uE006            |
| Storm    | 1007              | \uE007            |

## Resource Pack Setup

To enable fragment icons in the action bar, your resource pack must:

1. Have fragment textures in `assets/minecraft/textures/item/`
2. Define font mappings in `assets/minecraft/font/default.json`
3. Map each Unicode character to the corresponding fragment texture
4. Set appropriate `ascent` and `height` values for proper alignment

## Testing

To test if icons are working:
1. Activate a fragment
2. Check the action bar - you should see the fragment icon before the fragment name
3. If you see a square/missing character, the resource pack font mapping is missing
4. If you see nothing, check that the resource pack is loaded

## Notes

- Icons are colored using the fragment's color scheme
- The icon appears before all other action bar elements
- If the resource pack is not loaded, the Unicode character will display as a fallback square
- Character level is now hidden when mana system is disabled to reduce clutter
