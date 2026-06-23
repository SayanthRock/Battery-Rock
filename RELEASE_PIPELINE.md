# Battery-Rock APK Release Pipeline

Battery-Rock uses GitHub Actions to build and publish APK releases automatically.

---

## Pipeline Status

| Stage | Status | Purpose |
|---|---|---|
| Build release APK | Improved | Compiles the Android LSPosed module release APK |
| Collect APK files | Enabled | Finds generated APK files and prepares them for upload |
| Create release notes | Enabled | Creates clear release notes for each automated build |
| Upload workflow artifact | Enabled | Stores APK and checksum files inside the GitHub Actions run |
| Publish GitHub Release | Enabled | Uploads APK files to GitHub Releases for download |

---

## Current Improvement Focus

The release system has been improved to make APK builds cleaner and easier to publish.

### Included improvements

- Java 17 build environment
- Android SDK setup in GitHub Actions
- Automatic `local.properties` generation
- Xposed API preparation for CI builds
- APK artifact collection
- SHA256 checksum generation
- GitHub Release publishing
- Cleaner release notes

---

## Release Flow

```text
Push to main or run workflow manually
        ↓
Checkout repository
        ↓
Set up Java 17
        ↓
Set up Android SDK
        ↓
Prepare Gradle command
        ↓
Prepare local.properties
        ↓
Prepare Xposed API
        ↓
Build release APK
        ↓
Collect APK files
        ↓
Create release notes
        ↓
Upload workflow artifact
        ↓
Publish GitHub Release
```

---

## Notes

Battery-Rock is a root and LSPosed module. APK builds should be tested carefully on supported OPPO, Realme, and OnePlus firmware before daily use.

If a future GitHub Actions build fails, open the failed **Build release APK** step and check the first error under **What went wrong**. The bottom Gradle stack trace only shows that Gradle stopped, not the real cause.
