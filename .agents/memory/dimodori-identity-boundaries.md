---
name: DIMODORI identity boundaries
description: Branding, package identity, and compatibility boundaries for future DIMODORI changes.
---

DIMODORI uses one Android app identity, `com.dimodori.app`, for phones, tablets, and Android TV. The unified installer keeps separate touch and TV launcher activities/UIs. User-facing client branding and app-owned packages use DIMODORI, but Jellyfin/Emby protocol names, headers, API paths, provider IDs, shared-library namespaces, and third-party dependency coordinates must remain compatible.

**Why:** One Play listing and installer should serve every Android form factor without sacrificing D-pad behavior. A broad textual rename can still silently break media-server interoperability and third-party binaries.

**How to apply:** Keep one application module/ID, route TV through its dedicated Leanback activity, and preserve the separate TV UI package. Use neutral “media server” wording where provider names should not be visible. Before renaming an old identifier, determine whether it is app-owned branding or a protocol/library contract.

Incorporar actualizaciones de JellyCine de forma selectiva, sin reemplazar las
mejoras propias de DIMODORI.

**Why:** El usuario pidió aprovechar las actualizaciones «sin reemplazar las
mejoras que hicimos».

**How to apply:** Comparar cambios entre versiones de upstream y adaptar solo
los necesarios al fork; no sustituir módulos completos ni restaurar su
identidad, firma o estructura separada de móvil/TV.

Do not treat the launcher category as proof of device type: the generic entry
must also be safe on a TV, not just the Leanback entry.

**Why:** The user observed a new SHIELD installation showing touch UI on the
first opening, then TV UI after closing and reopening. Separate launcher
entries alone do not ensure the correct first-launch interface. This symptom
does not, by itself, prove that Play delivered a different package.

**How to apply:** Preserve the unified installer while selecting TV by actual
device capabilities before touch-only UI or effects. Keep package-delivery
investigations separate from activity-entry investigations.