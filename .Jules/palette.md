## 2024-07-25 - Non-Disruptive GUI Feedback

**Learning:** When a user performs an action in a GUI that fails due to a cooldown or other temporary restriction, closing the GUI is a disruptive experience. It forces the user to re-navigate to their previous state, causing unnecessary friction.

**Action:** For failed actions within a GUI, keep the interface open and provide immediate, non-disruptive feedback. In the `FragmentActivateGUI`, I removed the `player.closeInventory()` call and added a `playSound(Sound.ENTITY_VILLAGER_NO)` effect. This combination informs the user of the error without interrupting their workflow, allowing them to make a different choice seamlessly. This pattern should be applied to other GUIs to ensure a smoother user experience.
