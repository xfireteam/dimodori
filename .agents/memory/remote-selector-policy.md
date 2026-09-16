---
name: Remote selector policy
description: Product and failure-mode decisions for remotely controlling the DIMODORI server selector.
---

The selector control is intentionally public and unauthenticated. When enabled, the phone connection screen shows only manual connection; it must not remove presets or affect active sessions.

**Why:** The control is meant to be immediately accessible without credentials, while a remote outage or malformed response must never prevent users from choosing a server.

**How to apply:** Keep updates public unless the product decision is explicitly changed. Treat missing configuration, timeouts, non-success responses, invalid payloads, and an unconfigured endpoint as `hide=false`. Query only when entering the connection screen.