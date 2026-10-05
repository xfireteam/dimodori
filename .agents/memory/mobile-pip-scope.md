---
name: Mobile PiP scope
description: User intent and lifecycle boundaries for floating playback, distinct from background audio and TV.
---

Mobile floating playback means native Android PiP while the user browses other
applications, not an overlay permission, an in-app mini-player, or audio-only
background playback. Preserve both local playback engines and the TV entry
behavior.

**Why:** The user asked for the Emby-style floating window that can expand back
to the same uninterrupted video. A separate overlay or mandatory engine change
would add permissions/behavior they did not request.

**How to apply:** Keep one playback session across fullscreen/PiP, distinguish
a visible-but-paused activity from a stopped/hidden activity, and make explicit
close stop playback. Treat device resizing as system-controlled and do not
promise arbitrary window sizes or survival after force-stop.

Final progress must be captured while the player exists and be able to finish
after its owning screen is cleared.

**Why:** PiP dismissal can clear the player and cancel its screen scope before
the asynchronous stop report reaches the server, losing the user's resume
position even when the UI close seems correct.

**How to apply:** Keep final reports bounded and idempotent, preserve offline
progress, and test close/return with actual server state, not only window state.
