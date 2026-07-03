# Manual APK Release Workflow Request

This file documents the current release direction for Battery-Rock.

## Current request

- App: Battery-Rock
- Target version: 1.0.6
- Version code: 7
- Build type: Release APK
- Workflow file: `.github/workflows/manual-release-apk.yml`
- Workflow trigger: Manual only through GitHub Actions
- Upload method: Download workflow artifact, then upload APK manually to GitHub Releases
- Automatic GitHub Release publishing: Removed

## Manual workflow flow

1. Open GitHub Actions.
2. Select **Manual APK Release Build**.
3. Click **Run workflow**.
4. Enter release name, for example `v1.0.6`.
5. Wait for the build to finish.
6. Download the APK artifact.
7. Open GitHub Releases.
8. Draft a new release with tag `v1.0.6`.
9. Upload the APK file normally.
10. Publish the release.

## Local upload flow

1. Build the APK locally with Android Studio or terminal.
2. Confirm the release APK exists in `app/build/outputs/apk/release/`.
3. Rename the APK to a clear release name, for example `Battery-Rock-v1.0.6.apk`.
4. Upload the APK manually through GitHub Releases.

## Current fix status

- Automatic release publishing: removed.
- Manual APK workflow: added.
- README: updated for manual workflow and normal upload.
- Release version: kept at `1.0.6` / `7`.
- APK upload: handled manually through GitHub Releases.
