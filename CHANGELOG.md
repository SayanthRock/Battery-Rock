# Changelog

All notable Battery-Rock updates will be documented here.

---

## v1.0.8

### Launcher crash fix

- Added `BatteryRockStatus.kt`, a normal app startup class with no module API imports.
- Updated `MainActivity` to use `BatteryRockStatus.isModuleActive()` instead of loading the module entrypoint directly.
- Updated `BatteryRockInit` so the module active status is routed through `BatteryRockStatus` when the module is actually loaded.
- Updated APK version to `1.0.8` / versionCode `9`.

### Notes

This update targets the installed app crash shown as `Battery-Rock keeps stopping` by preventing the launcher process from loading the module entrypoint class directly.

---

## v1.0.7

### Internal APK fixes

- Hardened the in-app battery status reader with safe fallback values for ROMs that hide or limit battery details.
- Hardened the in-app performance reader so the dashboard can open even when system services return limited data.
- Cleaned Android manifest metadata and removed the unused boot permission.
- Updated APK version to `1.0.7` / versionCode `8`.
- Kept automatic GitHub Release publishing and the manual artifact workflow available.

### Notes

This update focuses on making the installed APK open safely and show useful status data across more OPPO, Realme and OnePlus ROM variants.

---

## v1.0.6

### Automatic GitHub Release workflow

- Added `.github/workflows/auto-release-apk.yml` for automatic APK builds and GitHub Release publishing.
- The automatic workflow builds the release APK, creates checksum and APK info files, uploads an Actions artifact, and publishes a GitHub Release.
- Kept `.github/workflows/manual-release-apk.yml` available for artifact-only builds.
- Updated README and release request documentation for both automatic and manual workflows.
- Kept APK version at `1.0.6` / versionCode `7`.

### Build and runtime cleanup

- Hardened the module entry point so the normal APK process reports inactive until the module actually loads.
- Routed framework, telemetry, and wakelock code through the safety controller.
- Replaced fragile hook-result assignments with explicit `setResult(...)` calls for better Kotlin compatibility.

### Notes

This update restores automatic GitHub Release publishing while keeping the manual artifact workflow available for testing.

---

## v1.0.4

### Comprehensive APK issue fixes

- Fixed Android battery power-source compatibility for release builds.
- Added an API guard for dock charging detection so Android 12 and Android 13 devices remain safe.
- Reduced the chance of release lint failure from newer Android battery constants.
- Bumped the APK to `1.0.4` / versionCode `5`.
- Kept the fully automated GitHub Actions release system active.
- APK builds continue to upload release APK, SHA256 checksum, APK metadata, and diagnostics artifacts.

### Notes

This release focuses on making the latest Battery Health and Phone Performance dashboard safer for public APK builds across supported Android versions.

---

## v1.0.3

### Battery backup improvement

- Added a live Battery-Rock device dashboard for battery backup monitoring.
- Added battery level, charging state, battery health, temperature, voltage, power source, and estimated capacity display.
- Added safer battery-health guidance based on Android battery status values.
- Added phone performance level calculation using CPU cores, Android API version, low-RAM status, and memory class.
- Added clearer UI cards for Battery Health and Phone Performance Level.
- Updated the app title/subtitle to reflect battery backup, battery health, and performance improvement.

### APK build and release fixes

- Bumped the APK to `1.0.3` / versionCode `4`.
- Removed the duplicate release workflow so APK publishing uses one main automated workflow.
- Kept automatic GitHub Release creation and APK asset upload through `.github/workflows/build-release.yml`.
- Kept SHA256 checksum, APK metadata, signature verification, and diagnostics artifact generation.

### Notes

This update improves the app dashboard and reduces APK automation confusion.

---

## v1.0.2

### Fully automated GitHub release system

- Added a zero-touch release flow for public APK upload.
- The workflow now reads `versionName` and `versionCode` directly from `app/build.gradle.kts`.
- Pushes to `main` automatically create a build tag like `v1.0.2-build.123`.
- Version tags like `v1.0.2` still publish clean stable releases.
- Manual workflow runs can still provide a custom release tag.
- Release notes are generated automatically from recent Git commits.
- APK files are renamed with the app name and release tag.
- `SHA256SUMS.txt` and `APK_INFO.txt` are generated automatically.
- APK signature verification runs before upload.
- APK artifacts and diagnostics artifacts are uploaded to GitHub Actions.
- Public GitHub Releases are published automatically with APK files attached.

### APK build fixes

- Confirmed the repository is public and ready for public APK release uploads.
- Stabilized the Compose BOM pin for local and CI builds.
- Bumped the APK version to `1.0.2` / versionCode `3`.
- Made the release APK installable from GitHub Actions by applying a signing config to the release build.
- Kept Java 17, Android SDK 35, AGP 8.7.3, Kotlin 2.0.21, and Gradle 8.9 aligned for stable CI builds.

---

## v1.0.1

### APK build fixes

- Improved GitHub Actions APK build flow.
- Added automatic API preparation for CI builds.
- Added fallback compile-only API stub generation when the official jar download is unavailable.
- Changed release build to Java 17 and Kotlin JVM target 17.
- Disabled release minify and resource shrinking for the first stable APK build flow.
- Simplified Compose UI dependencies to reduce missing icon dependency build failures.
- Updated APK build command to target `:app:assembleRelease` directly.
- Kept automatic artifact upload and GitHub Release publishing enabled.

---

## v1.0.0

### Initial project

- Added module entry point.
- Added framework hooks for background job and alarm control.
- Added telemetry package hooks.
- Added wakelock guard.
- Added Compose based module UI.
- Added recommended scope list for OPPO, Realme, and OnePlus packages.
