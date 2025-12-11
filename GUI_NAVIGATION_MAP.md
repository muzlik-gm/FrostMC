# GUI Navigation Map - FrostSMP Fragment System

## Main Hub: Fragment Overview GUI (`/fragment gui`)

```
┌─────────────────────────────────────────────────────────────┐
│                    FRAGMENT OVERVIEW                        │
│                     (/fragment gui)                         │
├─────────────────────────────────────────────────────────────┤
│  ╔═══════════════════════════════════════════════════════╗  │
│  ║  [Border: Gray Glass Panes]                           ║  │
│  ║                                                        ║  │
│  ║    🔥 💧 🌪️ 🌍 🌑 ✨ 🌀 👾 🐉 ⚡                      ║  │
│  ║    FIRE WATER AIR EARTH DARK LIGHT VOID MOB DRAGON    ║  │
│  ║    STORM (Fragments displayed in center area)         ║  │
│  ║                                                        ║  │
│  ║    Left-Click: View Abilities                         ║  │
│  ║    Right-Click: Activate Fragment                     ║  │
│  ║                                                        ║  │
│  ╚═══════════════════════════════════════════════════════╝  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              BOTTOM ROW NAVIGATION                    │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ [47]      [48]       [49]      [50]        [51]      │  │
│  │ 👁️        👢        📖         💎          🎮        │  │
│  │ SWITCH    CONTROLS   INFO      MANA        GIVE       │  │
│  │ FRAGMENT                       STATUS      (Admin)    │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## Navigation Tree

```
                    ┌─────────────────────┐
                    │  FRAGMENT OVERVIEW  │
                    │   (/fragment gui)   │
                    └──────────┬──────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐      ┌──────────────┐      ┌──────────────┐
│ LEFT-CLICK    │      │ RIGHT-CLICK  │      │ BOTTOM ROW   │
│ Fragment Icon │      │ Fragment Icon│      │ BUTTONS      │
└───────┬───────┘      └──────┬───────┘      └──────┬───────┘
        │                     │                      │
        ▼                     ▼                      │
┌───────────────┐      ┌──────────────┐             │
│ ABILITY       │      │ ACTIVATE     │             │
│ DETAILS GUI   │      │ FRAGMENT     │             │
│               │      │ (Direct)     │             │
└───────┬───────┘      └──────────────┘             │
        │                                            │
        │                                            │
        ▼                                            ▼
┌───────────────┐              ┌─────────────────────────────┐
│ [BACK]        │              │  5 BUTTON OPTIONS:          │
│ Returns to    │              ├─────────────────────────────┤
│ Overview      │              │ [47] Switch Fragment        │
└───────────────┘              │      → Activation GUI       │
                               │                             │
                               │ [48] Controls               │
                               │      → Control Scheme GUI   │
                               │                             │
                               │ [49] Info                   │
                               │      → Guide/Legend         │
                               │                             │
                               │ [50] Mana Status            │
                               │      → Mana Details GUI     │
                               │                             │
                               │ [51] Give Fragment (Admin)  │
                               │      → Admin Give GUI       │
                               └─────────────────────────────┘
```

---

## Detailed GUI Flows

### 1. Switch Fragment Flow
```
Fragment Overview → [Switch Fragment Button] → Fragment Activation GUI
                                                        │
                                                        ├─ Shows all owned fragments
                                                        ├─ Displays rank/level
                                                        ├─ Shows cooldown status
                                                        └─ Click to activate
```

### 2. Controls Flow
```
Fragment Overview → [Controls Button] → Control Scheme GUI
                                               │
                                               ├─ Sneak + Click
                                               ├─ Double Sneak
                                               ├─ Swap Hands
                                               ├─ Click Only
                                               └─ [Toggle Abilities]
```

### 3. Mana Status Flow
```
Fragment Overview → [Mana Status Button] → Mana Status GUI
                                                  │
                                                  ├─ Current Mana
                                                  ├─ Maximum Mana
                                                  ├─ Regen Rate
                                                  └─ Percentage
```

### 4. Admin Give Flow
```
Fragment Overview → [Give Fragment Button] → Player Selection GUI
                                                     │
                                                     ├─ Shows all online players
                                                     ├─ Player heads with info
                                                     └─ Click player
                                                           │
                                                           ▼
                                                  Fragment Selection GUI
                                                           │
                                                           ├─ Shows all 10 fragments
                                                           ├─ Ownership status
                                                           ├─ [Back] to players
                                                           └─ Click to give
```

### 5. Ability Details Flow
```
Fragment Overview → [Left-Click Fragment] → Ability Details GUI
                                                   │
                                                   ├─ Fragment stats panel
                                                   ├─ Level bonus panel
                                                   ├─ 4-5 ability icons
                                                   └─ [Back] to overview
```

---

## Button Reference

### Fragment Overview Bottom Row (Slots 45-53)

| Slot | Icon | Name | Function | Permission |
|------|------|------|----------|------------|
| 47 | 👁️ Ender Eye | Switch Fragment | Opens activation GUI | frostmc.fragment |
| 48 | 👢 Iron Boots | Controls | Opens control scheme GUI | frostmc.fragment |
| 49 | 📖 Book | Info | Shows guide/legend | frostmc.fragment |
| 50 | 💎 Lapis Lazuli | Mana Status | Opens mana details | frostmc.fragment |
| 51 | 🎮 Command Block | Give Fragment | Opens admin give GUI | fragment.admin |

---

## Alternative Access Methods

### Command Shortcuts
```
/fragment gui          → Main overview (hub)
/fragment activate     → Direct to activation GUI
/fragment controls     → Direct to control scheme GUI
/controls              → Direct to control scheme GUI
/fragment give         → Direct to admin give GUI (admin)
/fragment mana         → Direct to mana status GUI
```

### Direct Commands (Bypass GUI)
```
/fragment activate <FRAGMENT>           → Activate specific fragment
/fragment controls <scheme>             → Set control scheme
/fragment give <player> <fragment>      → Give fragment (admin)
```

---

## GUI Design Consistency

### Color Coding
- 🟢 **Green (§a)** - Active/Selected/Success
- ⚪ **White (§f)** - Available/Owned
- 🔴 **Red (§c)** - Locked/Cooldown/Error
- 🟡 **Yellow (§e)** - Highlight/Action
- 🔵 **Blue (§b)** - Mana/Water theme
- 🟣 **Purple (§d)** - Admin features

### Border Patterns
- **Fragment Overview:** Gray glass pane
- **Activation GUI:** Black glass pane
- **Control Scheme:** Gray glass pane
- **Admin Give:** Purple glass pane

### Button Placement Standards
- **Info buttons:** Slot 49 (bottom center)
- **Back buttons:** Slot 48 (bottom left-center)
- **Action buttons:** Slots 47-51 (bottom row)

---

## User Journey Examples

### New Player Journey
1. `/fragment gui` - Opens overview
2. Sees locked fragments (gray)
3. Clicks Info button - Learns about rituals
4. Completes ritual - Gets first fragment
5. Fragment appears in GUI (white)
6. Right-clicks to activate
7. Clicks Controls button - Sets preferred scheme
8. Left-clicks fragment - Views abilities

### Experienced Player Journey
1. `/fragment gui` - Opens overview
2. Clicks Switch Fragment button
3. Selects different owned fragment
4. Checks cooldown status
5. Uses Fragment Changer if needed
6. Activates new fragment
7. Returns to overview
8. Left-clicks to review abilities

### Admin Journey
1. `/fragment gui` - Opens overview
2. Clicks Give Fragment button (admin only)
3. Sees all online player heads
4. Clicks target player
5. Sees all 10 fragments
6. Clicks fragment to give
7. Target receives notification
8. GUI closes automatically

---

## Integration Points

### With Existing Systems
- ✅ Fragment ownership (FragmentManager)
- ✅ Cooldown tracking (CooldownManager)
- ✅ Mana system (ManaManager)
- ✅ Level/Rank progression (LevelManager/RankManager)
- ✅ Control preferences (PlayerPreferencesManager)
- ✅ Permissions (Bukkit permissions)

### Event Flow
```
Player Action → GUI Click Event → FragmentGUIListener
                                         │
                                         ├─ Validate action
                                         ├─ Check permissions
                                         ├─ Close current GUI
                                         ├─ 2-tick delay
                                         └─ Open target GUI
```

---

## Performance Considerations

- **Lazy Loading:** GUIs created on-demand
- **Efficient Updates:** Only refresh when needed
- **Memory Management:** Proper cleanup on close
- **Thread Safety:** All operations on main thread
- **Smooth Transitions:** 2-3 tick delays prevent glitches

---

## Accessibility Features

- **Clear Icons:** Recognizable materials for each function
- **Descriptive Names:** Small caps typography
- **Detailed Lore:** Explains what each button does
- **Visual Feedback:** Particles and sounds on actions
- **Consistent Layout:** Same positions across GUIs
- **Permission Checks:** Admin buttons only visible to admins

---

**Status:** ✅ Complete navigation system with 5 interconnected GUIs
**Build:** ✅ Successful compilation (189 source files)
**Integration:** ✅ Fully integrated with fragment system
**Documentation:** ✅ Comprehensive user and technical docs
