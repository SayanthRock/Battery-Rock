# Build Release APK Request

This file is used to trigger and document the automated GitHub Actions release APK workflow.

## Current request

- App: Battery-Rock
- Target version: 1.0.6
- Version code: 7
- Build type: Release APK
- Trigger reason: Resolve APK build and release workflow problems step by step
- Workflow: `.github/workflows/build-release.yml`

## Expected GitHub Actions flow

After this commit is pushed to `main`, GitHub Actions should automatically:

1. Prepare Java 17, Android SDK 35, Gradle 8.9, and the compile-only module API.
2. Build the release APK with full stacktrace output.
3. Save the full Gradle build log to `diagnostics/build-release-apk.log`.
4. Print the real Gradle or Kotlin error lines under `Important Gradle errors` if the build fails.
5. Verify APK signatures when build succeeds.
6. Collect APK files and metadata.
7. Generate SHA256 checksums.
8. Upload workflow artifacts and diagnostics.
9. Publish a GitHub Release with APK assets.

## Current fix status

- Release workflow diagnostics: improved.
- Compile-only API setup: kept automatic with stub fallback.
- Kotlin hook parameter imports: fixed.
- Runtime hook code: changed to safer method-hook style.
- Release version: updated to `1.0.6` / `7`.
