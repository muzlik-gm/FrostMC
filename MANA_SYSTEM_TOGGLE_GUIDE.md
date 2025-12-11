# Mana System Toggle - Usage Guide

## Problem Fixed
The mana system toggle wasn't working after `/reload` because the config was reloaded but the runtime state wasn't updated.

## Solution
Added a proper reload mechanism that updates both the config file AND the runtime state of the mana system.

---

## How to Toggle Mana System

### Step 1: Edit Config
Open `plugins/FrostSMP/config.yml` and change:

```yaml
# Set to false to disable mana system
mana_system_enabled: false
```

### Step 2: Reload Plugin
**IMPORTANT**: Use the Fragment command to reload, NOT `/reload`:

```
/fragment reload
```

This command will:
- ✅ Reload the config file
- ✅ Update mana system state (enable/disable)
- ✅ Update block manipulation settings
- ✅ Show confirmation message with mana system status

### Step 3: Verify
After running `/fragment reload`, you should see:
```
Configuration reloaded successfully
Mana System: DISABLED
```

---

## What Happens When Disabled

When `mana_system_enabled: false`:

### ❌ Disabled Features
- Mana costs for abilities (all abilities are FREE)
- Mana regeneration (stops completely)
- Mana UI display (hidden from action bar)
- Mana flasks (blocked from use)

### ✅ Still Working
- All abilities (work without mana cost)
- Fragment levels (cooldown reduction, damage bonus)
- Character levels (for other features)
- All other plugin features

---

## Important Notes

### DO NOT Use `/reload`
❌ **WRONG**: `/reload` or `/rl`
- This reloads the entire server
- Does NOT update mana system state
- Can cause issues

✅ **CORRECT**: `/fragment reload`
- Reloads only FrostSMP config
- Updates mana system state properly
- Safe and fast

### Permission Required
The `/fragment reload` command requires:
```
fragment.admin
```

### Server Restart Alternative
If you prefer, you can also:
1. Edit `config.yml`
2. Stop the server
3. Start the server

This will also work, but `/fragment reload` is faster.

---

## Testing Checklist

After disabling mana system:
- [ ] Use an ability - should work without mana cost
- [ ] Check action bar - mana display should be hidden
- [ ] Try to use mana flask - should be blocked
- [ ] Check Fragment GUI - mana info should be hidden
- [ ] Verify abilities still work normally

After re-enabling mana system:
- [ ] Use an ability - should consume mana
- [ ] Check action bar - mana display should be visible
- [ ] Use mana flask - should restore mana
- [ ] Verify mana regeneration is working

---

## Troubleshooting

### "Mana still showing after reload"
**Solution**: Make sure you used `/fragment reload`, not `/reload`

### "Command not working"
**Solution**: Check you have `fragment.admin` permission

### "Config changes not applying"
**Solution**: 
1. Verify the config file is in `plugins/FrostSMP/config.yml`
2. Check the YAML syntax is correct (no tabs, proper spacing)
3. Use `/fragment reload` after editing

### "Still consuming mana"
**Solution**: 
1. Check the config shows `mana_system_enabled: false`
2. Run `/fragment reload` again
3. Check console for any errors

---

## Console Output

When you run `/fragment reload`, the console will show:
```
[FrostSMP] Configuration loaded successfully
[FrostSMP] Plugin configuration reloaded
[FrostSMP] Mana System: DISABLED
```

This confirms the mana system state has been updated.

---

## Summary

**To disable mana system:**
1. Edit `config.yml`: Set `mana_system_enabled: false`
2. Run: `/fragment reload`
3. Verify: Check console and test abilities

**To re-enable mana system:**
1. Edit `config.yml`: Set `mana_system_enabled: true`
2. Run: `/fragment reload`
3. Verify: Check mana display and consumption

The fix is now live in the latest build. Copy the new JAR to your server and you're good to go!
