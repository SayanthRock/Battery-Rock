# Changelog

All notable Battery-Rock updates will be documented here.

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
