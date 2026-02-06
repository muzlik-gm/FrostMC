## 2026-02-06 - Fragment System Hardening

Vulnerability:
The plugin suffered from a CRITICAL item duplication exploit where players could withdraw fragments as physical items without losing the digital ownership of the fragment. Additionally, the high-power `ADMIN` fragment could be activated by unauthorized players if they obtained the item from an admin.

Learning:
Vulnerabilities often exist in "conversion" logic (digital state to physical item) when the state transition is not atomic or complete. In this case, `withdraw` only handled deactivation but not ownership removal. Furthermore, implicit trust in administrative items led to a lack of permission checks during activation.

Prevention:
1. Always implement state removal BEFORE granting physical item rewards in a "withdraw" pattern.
2. Implement permission guards at the lowest possible level (e.g., in the Manager class) for sensitive types or states, rather than just at the command layer.
