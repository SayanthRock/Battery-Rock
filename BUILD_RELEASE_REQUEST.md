# Automatic GitHub Release Workflow Request

This file documents the current release direction for Battery-Rock.

## Current request

- App: Battery-Rock
- Target version: 1.0.8
- Version code: 9
- Build type: Release APK
- Automatic workflow file: `.github/workflows/auto-release-apk.yml`
- Manual artifact workflow file: `.github/workflows/manual-release-apk.yml`
- Automatic GitHub Release publishing: Added
- Manual APK artifact workflow: Kept available

## Automatic release flow

1. Push code to `main`, push a tag like `v1.0.8`, or start the workflow manually.
2. The workflow reads `versionName` and `versionCode` from `app/build.gradle.kts`.
3. The workflow builds the release APK.
4. The workflow collects the APK, checksum file, and APK info file.
5. The workflow uploads the APK as an Actions artifact.
6. The workflow publishes a GitHub Release automatically.

## Manual artifact flow

1. Open GitHub Actions.
2. Select **Manual APK Release Build**.
3. Click **Run workflow**.
4. Enter release name, for example `v1.0.8`.
5. Download the APK artifact after the workflow finishes.

## Current fix status

- MainActivity no longer loads the module entrypoint directly.
- Added `BatteryRockStatus.kt` for normal app startup.
- Module active check now uses `BatteryRockStatus`.
- In-app device status reader: hardened.
- APK manifest metadata: cleaned.
- Automatic GitHub Release workflow: kept.
- Manual APK workflow: kept.
- Release version: updated to `1.0.8` / `9`.
