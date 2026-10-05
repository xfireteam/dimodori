---
name: TV distribution evidence
description: Distinguish Play Console configuration from approval and device-specific delivery before changing Android packaging.
---

A shared production track marked active and a compatible model in the general
device catalog do not establish TV quality approval or delivery eligibility for
the published app bundle. Verify the release-specific device catalog, explicit
TV review result and actual published binary before changing the manifest or ABI
filters to address an absent TV listing.

**Why:** A TV can accept other apps from the same account while DIMODORI is absent,
even when the general catalog and form-factor settings look correct. Speculative
packaging changes can break the intentional shared phone/TV identity without
addressing a distribution or review issue.

**How to apply:** Separate source audit findings from facts about the uploaded
binary and Console state. Treat unseen approval or AAB properties as unverified,
not as a successful check or a proven rejection. Verify the public store
description too; resources in app code do not certify the published store listing.
