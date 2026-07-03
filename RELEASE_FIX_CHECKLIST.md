# Release Fix Checklist

This checklist is used before publishing each Battery-Rock APK release.

## Current release target

- App: Battery-Rock
- Package: `dev.sayanthrock.batteryrock`
- Current version: `1.0.9`
- Current version code: `10`
- Main release workflow: `.github/workflows/auto-release-apk.yml`
- Manual artifact workflow: `.github/workflows/manual-release-apk.yml`

## Before release

1. Confirm `app/build.gradle.kts` has the correct `versionName` and `versionCode`.
2. Confirm the APK opens without the startup crash.
3. Confirm the dashboard shows battery, health, temperature and performance status.
4. Confirm fallback values show `Unknown` instead of closing the app when data is unavailable.
5. Confirm the UI theme is readable in dark mode.
6. Confirm the latest commit is pushed to `main`.

## GitHub Actions release checks

1. Open **Actions**.
2. Select **Automatic GitHub Release APK**.
3. Open the newest run from the latest commit.
4. Confirm the APK build step finishes successfully.
5. Confirm APK artifact upload finishes successfully.
6. Confirm GitHub Release publishing finishes successfully.
7. Open **Releases** and confirm the newest release includes:
   - APK file
   - `SHA256SUMS.txt`
   - `APK_INFO.txt`

## If release fails

Check the first real error above the final `exit code 1` line. The final line only confirms the process failed; it is not the root cause.

Common places to check:

- Gradle or Kotlin compile error
- Android resource linking error
- Missing APK output file
- GitHub Release permission error
- Duplicate tag or release name
- Artifact upload issue

## Current status

- Startup crash fix: completed in v1.0.8.
- UI and theme refresh: completed in v1.0.9.
- Automatic release workflow: enabled.
- Manual artifact workflow: available.
