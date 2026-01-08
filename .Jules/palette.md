## 2024-07-29 - In-Place GUI Refresh
**Learning:** Closing a GUI immediately after a user action, whether successful or not, is a jarring experience. It forces the user to re-open the interface to see the updated state or perform another action, which interrupts their workflow.
**Action:** In GUIs where a user can perform multiple actions or see changing states (like cooldowns or active selections), I will refresh the GUI content in-place instead of closing it. This provides immediate, non-disruptive feedback and keeps the user in control.
