---
name: TV remote menu input
description: Input ownership and focus decisions for remote-controlled TV selectors.
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
