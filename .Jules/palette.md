## 2026-02-03 - [Flicker-free GUI Navigation]
**Learning:** In Bukkit, explicit `player.closeInventory()` calls followed by a delayed `openInventory()` cause a visual flicker. Omitting the close call and using a 1-tick delay allows the new inventory to replace the old one seamlessly.
**Action:** Always omit `player.closeInventory()` when navigating directly between same-sized GUIs or when the new GUI should immediately replace the old one.

## 2026-02-03 - [Centralized Close Button]
**Learning:** Custom Minecraft GUIs often lack an explicit 'Close' button, forcing users to rely on the ESC key. Providing a consistent Close button in the bottom-right corner (slot 53) improves accessibility and discoverability.
**Action:** Standardize a Close button in the bottom-right corner for all multi-row custom GUIs.
