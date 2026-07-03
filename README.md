<div align="center">

# Battery-Rock

### OPPO · Realme · OnePlus Battery Backup Improvement Module

**Battery Backup · Battery Health · Phone Performance Level · Manual APK Release Workflow**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.0.6-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)

Battery-Rock is an Android module-style app for OPPO, Realme, and OnePlus devices. It includes a dark Compose dashboard for battery status, battery health, phone performance level, and supported package scope guidance.

</div>

---

## Latest Update, v1.0.6

This update keeps automatic release publishing removed, but adds a manual GitHub Actions workflow that can build the APK artifact on demand. The APK can then be uploaded normally to GitHub Releases.

| Area | Status |
|---|---|
| APK version | `1.0.6` |
| Version code | `7` |
| Android target | Android 12+ / SDK 35 |
| Kotlin | `2.0.21` |
| AGP | `8.7.3` |
| Automatic release publishing | Removed |
| Manual APK workflow | Added |
| GitHub Release upload | Manual |

---

## What Battery-Rock Does

The dashboard shows:

- Battery percentage
- Charging state
- Battery health
- Battery temperature
- Voltage
- Power source
- Estimated capacity when Android exposes the data
- Phone performance level
- Android version and app memory class

Device behavior depends on ROM version, installed packages, signal strength, kernel behavior, and selected scope. Test on your own device before daily use.

---

## Main Features

| Area | Battery-Rock Action |
|---|---|
| Module entry point | Java-layer runtime hook entry declared in `assets/xposed_init` |
| Battery health | Reads Android battery health, level, temperature, power source, and capacity estimate |
| Phone performance level | Calculates a clear status from CPU cores, Android API, low-RAM state, and memory class |
| Compose UI | Dark, clean dashboard with battery and performance cards |
| Scope list | Recommended package list in `res/xml/scope.xml` |
| APK release | Build with the manual workflow or locally, then upload manually to GitHub Releases |

---

## Manual APK Release Workflow

Workflow file:

```text
.github/workflows/manual-release-apk.yml
```

This workflow does **not** publish a GitHub Release automatically. It only builds the release APK and uploads the APK as a workflow artifact. You can then download the artifact and upload it normally to GitHub Releases.

### Run the workflow

1. Open the repository on GitHub.
2. Go to **Actions**.
3. Select **Manual APK Release Build**.
4. Click **Run workflow**.
5. Enter a release name such as `v1.0.6`.
6. Download the uploaded artifact after the workflow finishes.
7. Upload the APK manually in GitHub Releases.

---

## Normal Local APK Build and Upload

Use Android Studio or terminal:

```bash
gradle :app:assembleRelease
```

The release APK will be generated inside:

```text
app/build/outputs/apk/release/
```

Recommended APK name format:

```text
Battery-Rock-v1.0.6.apk
```

### Upload normally to GitHub Releases

1. Open the repository on GitHub.
2. Go to **Releases**.
3. Click **Draft a new release**.
4. Create a tag such as `v1.0.6`.
5. Upload the APK file.
6. Add release notes.
7. Publish the release.

No automatic release publishing is required. Tiny mercy from the YAML mines.

---

## Project Structure

```text
Battery-Rock/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/xposed_init
│       ├── java/dev/sayanthrock/batteryrock/
│       │   ├── BatteryRockInit.kt
│       │   ├── DeviceStatusReader.kt
│       │   ├── MainActivity.kt
│       │   ├── hooks/
│       │   │   ├── AutoHookControllerEngine.kt
│       │   │   ├── FrameworkHook.kt
│       │   │   ├── RomAdaptiveEngine.kt
│       │   │   ├── TelemetryKiller.kt
│       │   │   └── WakelockGuard.kt
│       │   └── ui/theme/Theme.kt
│       └── res/xml/scope.xml
├── .github/workflows/manual-release-apk.yml
├── BUILD_RELEASE_REQUEST.md
├── CHANGELOG.md
└── README.md
```

---

## Core Files

| File | Purpose |
|---|---|
| `BatteryRockInit.kt` | Runtime module entry point loaded from `assets/xposed_init` |
| `DeviceStatusReader.kt` | Reads safe Android battery health and device performance status |
| `MainActivity.kt` | Compose UI with module status, battery health, performance level, and package list |
| `AutoHookControllerEngine.kt` | Safety controller for runtime hook activation |
| `RomAdaptiveEngine.kt` | Detects ROM profile and conservative runtime behavior |
| `FrameworkHook.kt` | Framework-level scheduling and alarm handling code |
| `TelemetryKiller.kt` | Scoped package runtime handling code |
| `WakelockGuard.kt` | Wake lock duration handling code |
| `manual-release-apk.yml` | Manual build workflow that uploads APK artifacts without auto-publishing a release |

---

## Local Build Note

For local Android Studio or terminal builds, place the compile-only API jar here when required by your local environment:

```text
app/libs/api-82.jar
```

Then build with:

```bash
gradle :app:assembleRelease
```

---

## Safety Note

Battery-Rock is for advanced Android users testing on devices they own and control. Use recommended scopes carefully and test each ROM/device setup before relying on it daily.
