---
name: DIMODORI identity boundaries
description: Branding, package identity, and compatibility boundaries for future DIMODORI changes.
---

DIMODORI uses one Android app identity, `com.dimodori.app`, for phones, tablets, and Android TV. The unified installer keeps separate touch and TV launcher activities/UIs. User-facing client branding and app-owned packages use DIMODORI, but Jellyfin/Emby protocol names, headers, API paths, provider IDs, shared-library namespaces, and third-party dependency coordinates must remain compatible.

**Why:** One Play listing and installer should serve every Android form factor without sacrificing D-pad behavior. A broad textual rename can still silently break media-server interoperability and third-party binaries.

**How to apply:** Keep one application module/ID, route TV through its dedicated Leanback activity, and preserve the separate TV UI package. Use neutral “media server” wording where provider names should not be visible. Before renaming an old identifier, determine whether it is app-owned branding or a protocol/library contract.