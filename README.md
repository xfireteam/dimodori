# DIMODORI multiapp workspace

This repository separates the existing native DIMODORI client from web artifacts
and shared services:

- `apps/dimodori-android/` contains the unchanged Gradle project for Android and
  its existing iOS/Kotlin Multiplatform support files.
- `artifacts/` is reserved for independently runnable and publishable Replit
  applications, including the future web panel.
- `lib/` contains shared packages for web artifacts and services.

## Android commands

The original Gradle commands still work from the Android project directory:

```bash
cd apps/dimodori-android
./gradlew :phone:assembleDebug
./gradlew :phone:bundleRelease
```

Equivalent commands are available from the repository root:

```bash
pnpm run android:tasks
pnpm run android:assemble-debug
pnpm run android:bundle-release
```

The root `codemagic.yaml` builds the same `:phone` application module from its
new location and collects the APK and AAB from the relocated output paths.
Signing variables, application identity, versioning, Gradle modules, resources,
and release behavior remain owned by `apps/dimodori-android/`.

For full Android, iOS, signing, and release documentation, see
[`apps/dimodori-android/README.md`](apps/dimodori-android/README.md).