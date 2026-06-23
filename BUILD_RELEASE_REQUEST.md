# Build Release APK Request

This file is used to trigger the fully automated GitHub Actions release APK workflow.

## Current request

- App: Battery-Rock
- Target version: 1.0.4
- Version code: 5
- Build type: Release APK
- Trigger reason: Manual user request to build and upload APK through GitHub Releases
- Workflow: `.github/workflows/build-release.yml`

After this commit is pushed to `main`, GitHub Actions should automatically:

1. Build the release APK.
2. Verify APK signatures.
3. Collect APK files.
4. Generate SHA256 checksums.
5. Generate APK metadata.
6. Upload workflow artifacts.
7. Publish a GitHub Release with APK assets.
