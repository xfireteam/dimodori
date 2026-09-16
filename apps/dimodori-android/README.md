<p align="center">
  <img src="phone/src/main/assets/dimodori_logo.png" alt="DIMODORI" width="200">
</p>

<h1 align="center">DIMODORI</h1>

<p align="center">
   The official DIMODORI media client — designed for phone, TV, and beyond.
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.dimodori.app">
    <img src="https://img.shields.io/badge/Google_Play-Download-34A853?style=for-the-badge&logo=google-play&logoColor=white" alt="Google Play">
  </a>
  <a href="https://dimodori.com">
    <img src="https://img.shields.io/badge/DIMODORI-Website-111111?style=for-the-badge" alt="DIMODORI website">
  </a>
</p>

<p align="center">
  <a href="https://dimodori.com">dimodori.com</a>
</p>

---

## Features

### Playback

- **Custom MPV player** set as default engine — built from a custom fork with HDR10/HDR10+/Dolby Vision support and format badges
- HDR passthrough with dynamic dataspace switching and headroom hints (Android 14+)
- Configurable tone-mapping, rendering quality, and GPU filter settings
- Audio passthrough (TrueHD, DTS-HD, Atmos) when supported by device/output
- Spatial audio passthrough on compatible devices
- Media3 ExoPlayer fallback when MPV cannot render
- FFmpeg extension integration for broad codec coverage
- In-player quality selection, audio transcoding controls, and configurable player cache
- Gesture controls (seek, volume, brightness), lock mode, and start-maximized preference
- Skip Intro button when IntroDB/TheIntroDB markers are available
- Track selection dialog with format badges and AMOLED styling
- Subtitle styling controls with improved track handling
- Google Cast with inline remote playback controls

### Discovery

- **In-app Trailers** with autoplay in feature carousel (capped to 720p on phones)
- Trailers & Extras section on detail screens
- **For You** personalized recommendations with watched feed
- Awards category powered by Wikidata
- Immersive search with suggestions, live results, and categorized output
- Favorites tab with compact header and view-all navigation

### Seerr Integration

- Discovery, search, recommendations, and detail pages
- Request badges, request limits, and title requests
- Trailer support for Seerr detail items

### Downloads

- Offline downloads with queue, pause/resume/cancel, and persistent state recovery
- **Transcoded download support** with quality picker
- Audio track selection in download quality picker
- Season and series download with storage estimation
- Offline-aware navigation — falls back to downloaded content when network is unavailable

### TV

- Redesigned TV UI with D-pad navigation and remote control support
- Cinematic full-bleed detail screen overlay
- Immersive backdrop for suggestions
- Keyboard overlay search with carousel results
- Feature hero card with card expand and backdrop crossfade

### Screen Time

- Watch activity stats with daily breakdown charts
- Watched items list with poster images and media type filter
- Dual chart filters for media type and time range
- Week and month navigation for historical viewing
- Tablet-optimized layout

### Multi-Server & Connections

- Media server support with automatic endpoint resolution
- Merge-version support with local version selection (no server-side changes required)
- **Admin Panel** with live server info, now-playing sessions, and activity log

### Platforms

| Platform | Status |
|----------|--------|
| Android Phone | Stable |
| Android TV | Stable |
| iOS | In Development |

---

## Screenshots

<div align="center">
  <img src="docs/screenshots/home.jpg" alt="Home" width="30%" style="max-width:200px;min-width:100px;margin:5px" />
  <img src="docs/screenshots/details.jpg" alt="Details" width="30%" style="max-width:200px;min-width:100px;margin:5px" />
  <img src="docs/screenshots/search-immersive.jpg" alt="Search" width="30%" style="max-width:200px;min-width:100px;margin:5px" />
</div>

<div align="center">
  <img src="docs/screenshots/viewall.jpg" alt="View All" width="30%" style="max-width:200px;min-width:100px;margin:5px" />
  <img src="docs/screenshots/settings.jpg" alt="Settings" width="30%" style="max-width:200px;min-width:100px;margin:5px" />
  <img src="docs/screenshots/searchscreen.jpg" alt="Search Results" width="30%" style="max-width:200px;min-width:100px;margin:5px" />
</div>

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.3, Coroutines, Flow |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt + KSP |
| Networking | Ktor Client + OkHttp 5 |
| Images | Coil 3 |
| Player | MPV (primary), Media3 ExoPlayer (fallback) |
| Multiplatform | Kotlin Multiplatform (Android + iOS) |

## Project Structure

```
phone/   — Unified Android app module (phone/tablet + Android TV)
           └─ TV UI lives under com.dimodori.app.tv
data/    — APIs, repositories, models; multiplatform networking
core/    — Shared player, preferences, and utilities
shared/  — Shared UI components and image infrastructure
```

---

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17
- Android SDK API 36

### Build

```bash
# Unified Android app for phone, tablet, and TV
./gradlew :phone:assembleDebug

# Google Play bundle
./gradlew :phone:bundleRelease
```

APK naming: `dimodori-{debug|release}-<version>.apk`

The same package, `com.dimodori.app`, contains separate launcher activities
for touch devices and Android TV. Android TV opens the dedicated D-pad UI
through `LEANBACK_LAUNCHER`.

### Codemagic: signed APK and AAB

The root `codemagic.yaml` defines the manual workflow
**DIMODORI Android - APK and AAB**. It builds the single `:phone` application
module and produces:

- `phone/build/outputs/apk/release/dimodori-release-<version>.apk` for manual
  installation and testing;
- `phone/build/outputs/bundle/release/phone-release.aab` for Google Play.

Because `com.dimodori.app` has not been published before, generate its first
signing identity from the repository root:

```bash
chmod +x scripts/generate-android-signing.sh
./scripts/generate-android-signing.sh
```

The script creates `.signing/` locally with restrictive file permissions. It
asks for a key alias and hidden passwords. Press Enter at either password
prompt to generate a strong random value automatically. It does not print any
secret value. Back up the generated keystore and
`codemagic-secrets.txt` securely: losing them can prevent future app updates.

Before starting the workflow, create an environment variable group in
Codemagic named `dimodori_android_release`. Open
`.signing/codemagic-secrets.txt`, add its four variables to that group, and
mark every one **Secret**:

| Variable | Value |
| --- | --- |
| `DIMODORI_KEYSTORE_BASE64` | Base64 contents of the generated DIMODORI Android keystore |
| `DIMODORI_STORE_PASSWORD` | Keystore password |
| `DIMODORI_KEY_ALIAS` | Signing key alias |
| `DIMODORI_KEY_PASSWORD` | Signing key password |

Do not add `DIMODORI_STORE_FILE` in Codemagic. The workflow restores the
keystore in a private build directory and sets that path automatically.

The public certificate fingerprint is written to
`.signing/certificate-sha256.txt`. Register that SHA-256 fingerprint in Android
Developer Verification. The fingerprint is public metadata; the other files
inside `.signing/` are private.

To build:

1. Commit and push `codemagic.yaml` with the rest of the source code.
2. In Codemagic, open **Environment variables**, create the
   `dimodori_android_release` group, and add the four values generated by the
   signing script.
3. Click **Check for configuration files** if the workflow is not listed yet.
4. Click **Start new build**, select the branch and
   **DIMODORI Android - APK and AAB**, then start the build manually.
5. Download the APK and AAB from the build's **Artifacts** section.

The workflow has no automatic trigger. It validates the signing variables,
keystore password, and alias before running Gradle. Private signing files and
credentials remain excluded by `.gitignore` and must never be committed.

---

## Website

Visit [dimodori.com](https://dimodori.com) for support and product information.

---

## Contributing

Issues and pull requests are welcome. For large feature work, open an issue first to align on scope.

For community discussions and support, see [Discussions](https://dimodori.com).

---

## Privacy

See [PRIVACY](PRIVACY) for the current privacy policy.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).