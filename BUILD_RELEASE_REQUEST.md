# Normal APK Upload Request

This file documents the current release direction for Battery-Rock.

## Current request

- App: Battery-Rock
- Target version: 1.0.6
- Version code: 7
- Build type: Release APK
- Upload method: Normal manual upload
- Automated GitHub Actions release workflow: Removed

## Manual upload flow

1. Build the APK locally with Android Studio or terminal.
2. Confirm the release APK exists in `app/build/outputs/apk/release/`.
3. Rename the APK to a clear release name, for example `Battery-Rock-v1.0.6.apk`.
4. Open GitHub Releases.
5. Draft a new release with tag `v1.0.6`.
6. Upload the APK file normally.
7. Add release notes.
8. Publish the release.

## Current fix status

- Automated APK release workflow: removed.
- README: updated for normal manual upload.
- Release version: kept at `1.0.6` / `7`.
- APK upload: handled manually through GitHub Releases.
