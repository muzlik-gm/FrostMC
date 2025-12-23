# Texture Debug Guide

## Issue Summary
Custom UI textures (back button, stats, bonus, controls) are not displaying in the Fragment GUI.

## What Was Fixed

1. **Pack Format**: Changed from 34 to 15 (correct for Minecraft 1.20.4)
2. **Resource Pack**: Rebuilt `FrostMC.zip` with correct structure
3. **Debug Command**: Added `/testtexture` command to test custom model data

## Testing Steps

### Step 1: Apply Resource Pack
1. Copy `FrostMC_Resourcepack.zip` to your `.minecraft/resourcepacks/` folder
2. In Minecraft: Options → Resource Packs → Enable "FrostMC"
3. Click "Done" to reload
4. Rejoin the server

### Step 2: Test with Debug Command
Run these commands in-game to get test items:

```
/testtexture ui_back_button
/testtexture ui_stats
/testtexture ui_bonus
/testtexture ui_controls
```

Each command will give you a paper item with the custom model data applied.

**Expected Result**: You should see custom textures instead of plain paper
**If you see plain paper**: The resource pack isn't loading correctly

### Step 3: Check Fragment GUI
1. Run `/fragment` command
2. Click on any fragment to view abilities
3. Check these buttons:
   - **Back button** (bottom center) - Should show back arrow texture
   - **Stats panel** (top left) - Should show stats icon
   - **Bonus panel** (top center-left) - Should show bonus icon
   - **Controls button** (bottom row in main GUI) - Should show controls icon

## Troubleshooting

### Resource Pack Not Loading?

**Check F3 Debug Screen**:
1. Press F3 in-game
2. Look for "Resource Packs:" on the right side
3. Should show "FrostMC" in the list

**Force Reload**:
- Press F3 + T to reload resource packs
- Or restart Minecraft completely

**Check Pack Format**:
1. Open `FrostMC_Resourcepack.zip`
2. Open `pack.mcmeta`
3. Should show: `"pack_format": 15`

### Still Seeing Plain Paper?

**Verify Custom Model Data**:
The `/testtexture` command shows the custom model data value in the lore:
- ui_back_button = 4000
- ui_stats = 4001
- ui_bonus = 4002
- ui_controls = 4003

**Check Resource Pack Files**:
Inside `FrostMC_Resourcepack.zip`, verify these files exist:
```
assets/minecraft/textures/item/ui/back.png
assets/minecraft/textures/item/ui/stats.png
assets/minecraft/textures/item/ui/bonus.png
assets/minecraft/textures/item/ui/controls.png
assets/minecraft/models/item/ui/back.json
assets/minecraft/models/item/ui/stats.json
assets/minecraft/models/item/ui/bonus.json
assets/minecraft/models/item/ui/controls.json
assets/minecraft/models/item/paper.json (with overrides)
pack.mcmeta (pack_format: 15)
```

### Server-Side Resource Pack

If you want the server to automatically apply the resource pack:

1. Host `FrostMC_Resourcepack.zip` on a web server or file host
2. Edit `server.properties`:
   ```properties
   resource-pack=https://your-url.com/FrostMC_Resourcepack.zip
   resource-pack-sha1=<SHA1 hash of the ZIP file>
   require-resource-pack=true
   ```
3. Restart the server

To get SHA1 hash:
```powershell
Get-FileHash -Algorithm SHA1 FrostMC_Resourcepack.zip
```

## Technical Details

### Custom Model Data Mapping
```
TextureRegistry.java:
  "ui_back_button" → 4000
  "ui_stats" → 4001
  "ui_bonus" → 4002
  "ui_controls" → 4003

paper.json overrides:
  4000 → item/ui/back
  4001 → item/ui/stats
  4002 → item/ui/bonus
  4003 → item/ui/controls

Model files reference textures:
  item/ui/back.json → item/ui/back.png
  item/ui/stats.json → item/ui/stats.png
  item/ui/bonus.json → item/ui/bonus.png
  item/ui/controls.json → item/ui/controls.png
```

### Why Pack Format Matters
- Minecraft 1.20-1.20.4: Pack Format **15**
- Minecraft 1.20.5-1.20.6: Pack Format **32**
- Minecraft 1.21+: Pack Format **34**

Using the wrong pack format will cause Minecraft to reject the resource pack entirely.

## Next Steps

1. **Test the debug command** to verify custom model data is working
2. **Check if textures appear** on the test items
3. **Open Fragment GUI** to see if buttons show custom textures
4. **Report back** with results:
   - Do test items show custom textures?
   - Do GUI buttons show custom textures?
   - Any errors in console/logs?

## Files Updated
- `resourcepack/pack.mcmeta` (pack_format: 15)
- `resourcepack/FrostMC.zip` (rebuilt)
- `FrostMC_Resourcepack.zip` (rebuilt)
- `src/main/java/com/muzlik/command/TestTextureCommand.java` (new)
- `src/main/resources/plugin.yml` (added testtexture command)
- `src/main/java/com/muzlik/FrostSMPPlugin.java` (registered command)

---

**Status**: Ready for testing
**Plugin**: Deployed to server
**Resource Pack**: Ready for distribution
