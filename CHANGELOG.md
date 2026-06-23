# Changelog

All notable Battery-Rock updates will be documented here.

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
- Added phone performance level calculation using CPU cores, Android API version, low-RAM status, and app memory class.
- Added clearer UI cards for Battery Health and Phone Performance Level.
- Updated the app title/subtitle to reflect battery backup, battery health, and performance improvement.

### APK build and release fixes

- Bumped the APK to `1.0.3` / versionCode `4`.
- Removed the duplicate release workflow so APK publishing uses one main automated workflow.
- Kept automatic GitHub Release creation and APK asset upload through `.github/workflows/build-release.yml`.
- Kept SHA256 checksum, APK metadata, signature verification, and diagnostics artifact generation.

### Notes

This update improves the app dashboard and reduces APK automation confusion. Battery-Rock remains an LSPosed module, so users should enable only the recommended scopes and test on their own OPPO, Realme, or OnePlus ROM.

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

### Notes

This release is focused on fixing build, upload, and public release automation problems. Battery-Rock is still a root and LSPosed module, so users should test scope packages carefully on their own OPPO, Realme, or OnePlus ROM.

---

## v1.0.1

### APK build fixes

- Improved GitHub Actions APK build flow.
- Added automatic Xposed API preparation for CI builds.
- Added fallback compileOnly Xposed API stub generation when the official jar download is unavailable.
- Changed release build to Java 17 and Kotlin JVM target 17.
- Disabled release minify and resource shrinking for the first stable APK build flow.
- Simplified Compose UI dependencies to reduce missing icon dependency build failures.
- Updated APK build command to target `:app:assembleRelease` directly.
- Kept automatic artifact upload and GitHub Release publishing enabled.

### Improvements

- Updated Battery-Rock README with current build status.
- Added clearer setup instructions for GitHub Actions and local builds.
- Added safer troubleshooting guidance for LSPosed and APK build problems.
- Added an improvement plan for future releases.

### Notes

Battery-Rock is a root and LSPosed module. Device behavior depends on ROM version, installed packages, kernel behavior, and selected LSPosed scope. Test carefully before using it as a daily driver.

---

## v1.0.0

### Initial project

- Added LSPosed module entry point.
- Added framework hooks for background job and alarm control.
- Added telemetry package hooks.
- Added wakelock guard.
- Added Compose based module UI.
- Added recommended LSPosed scope list for OPPO, Realme, and OnePlus packages.
