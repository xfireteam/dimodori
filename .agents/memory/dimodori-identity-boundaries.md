---
name: DIMODORI identity boundaries
description: Branding, package identity, and compatibility boundaries for future DIMODORI changes.
---

DIMODORI is published as a new app identity: `com.dimodori.app` for phones and `com.dimodori.tv` for TV. User-facing client branding and app-owned packages use DIMODORI, but Jellyfin/Emby protocol names, headers, API paths, provider IDs, shared-library namespaces, and third-party dependency coordinates must remain compatible.

**Why:** A broad textual rename can silently break media-server interoperability and third-party binaries. Phone and TV also intentionally retain separate application IDs.

**How to apply:** Use neutral “media server” wording where provider names should not be visible. Before renaming an old identifier, determine whether it is app-owned branding or a protocol/library contract.