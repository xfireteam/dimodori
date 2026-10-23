---
name: TV remote menu input
description: Input ownership and focus decisions for TV selectors and text-field controls.
---

For a TV menu opened with OK/center/Enter, finish the opening press before
transferring focus. Give one handler ownership of both key phases; a repeat,
canceled press, or release inherited by a new popup must not confirm an option.
Keep Back local to the open menu and restore focus to its originating control.

**Why:** The user reported that Audio/Subtitles on the TV detail page folded
away immediately and prevented selection. The repair deliberately avoids
mixing editable-dropdown expansion with separate manual key activation.
The precise device-side dismissal callback was not confirmed without Android
hardware; do not describe the source-level diagnosis as a device trace.

**How to apply:** Preserve these interaction requirements when changing TV
selectors, and verify complete physical press/release and held-key sequences,
not just click callbacks or source contracts. Distinguish pre-playback
selectors from the separate dialogs inside the player.

For auxiliary TV text-field controls, preserve directional navigation inside
the open software keyboard. Provide an explicit remote route to the control
after the keyboard closes, rather than taking arrow keys away from editing.

**Why:** The user reported that the password eye worked on phone but could not
be selected on TV. The chosen fix isolates its focus target while deliberately
leaving the open TV keyboard's navigation unchanged; phone behavior is not
evidence of TV remote compatibility.

**How to apply:** Keep this boundary when adding controls next to TV editors.
Check keyboard dismissal, return to editing, and focus on an actual TV or
emulator; source checks alone cannot establish device keyboard behavior.
