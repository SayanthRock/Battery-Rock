<div align="center">

# Battery-Rock

### Battery health and performance dashboard for OPPO, Realme and OnePlus

**Battery status · Phone performance level · Safe app startup · APK release workflows**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.0.8-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Releases](https://img.shields.io/badge/GitHub-Releases-111827?style=for-the-badge&logo=github)](https://github.com/SayanthRock/Battery-Rock/releases)

Battery-Rock is an Android app dashboard for battery health, charging status, temperature, estimated capacity, phone performance level and device status guidance.

</div>

---

## Latest update: v1.0.8

This update focuses on fixing the installed APK startup problem shown as **Battery-Rock keeps stopping**.

| Area | Status |
|---|---|
| APK version | `1.0.8` |
| Version code | `9` |
| Package | `dev.sayanthrock.batteryrock` |
| Minimum Android | Android 12 / API 31 |
| Target SDK | Android 15 / API 35 |
| Kotlin | `2.0.21` |
| AGP | `8.7.3` |
| Java toolchain | `17` |
| Automatic release workflow | Enabled |
| Manual APK artifact workflow | Available |

### What changed in v1.0.8

- Added `BatteryRockStatus.kt`, a safe startup bridge for the normal APK process.
- Updated `MainActivity` so the app opens through the safe status bridge.
- Updated the status check routing used by the background module entrypoint.
- Kept the hardened battery and performance readers from v1.0.7.
- Bumped the APK to `1.0.8` / versionCode `9`.

---

## Features

| Feature | Description |
|---|---|
| Battery dashboard | Shows battery percentage, charging state, health, temperature, voltage and power source. |
| Capacity estimate | Shows estimated battery capacity when Android exposes enough data. |
| Performance level | Calculates a simple phone performance level from CPU cores, Android API and memory class. |
| Safe fallbacks | Shows `Unknown` instead of closing when a device hides battery or performance details. |
| Status card | Shows whether the status bridge reports active or inactive. |
| Modern UI | Dark Compose interface with rounded cards and clean spacing. |
| Release automation | Builds APKs and publishes GitHub Releases automatically. |

---

## Download

Open the releases page and download the latest APK:

```text
https://github.com/SayanthRock/Battery-Rock/releases
```

Recommended APK file name:

```text
Battery-Rock-v1.0.8.apk
```

---

## Automatic GitHub Release workflow

Workflow file:

```text
.github/workflows/auto-release-apk.yml
```

This workflow builds the release APK and publishes a GitHub Release automatically.

It runs when:

- code is pushed to `main`
- a tag like `v1.0.8` is pushed
- the workflow is started manually from GitHub Actions

For normal pushes to `main`, the workflow creates a build tag like:

```text
v1.0.8-build.123
```

The release contains:

- release APK
- `SHA256SUMS.txt`
- `APK_INFO.txt`
- generated release notes

---

## Manual APK artifact workflow

Workflow file:

```text
.github/workflows/manual-release-apk.yml
```

This workflow builds the APK and uploads it as a GitHub Actions artifact without publishing a release.

---

## Local build

Requirements:

| Tool | Version |
|---|---|
| JDK | 17 |
| Android SDK | 35 |
| Gradle | 8.9 |
| Kotlin | 2.0.21 |

Build release APK:

```bash
gradle :app:assembleRelease
```

Release APK output:

```text
app/build/outputs/apk/release/
```

---

## Project structure

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
│       │   ├── BatteryRockStatus.kt
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
├── .github/workflows/auto-release-apk.yml
├── .github/workflows/manual-release-apk.yml
├── BUILD_RELEASE_REQUEST.md
├── CHANGELOG.md
└── README.md
```

---

## Important files

| File | Purpose |
|---|---|
| `MainActivity.kt` | Compose dashboard UI. Uses the safe status bridge. |
| `BatteryRockStatus.kt` | Safe status bridge for normal app startup. |
| `BatteryRockInit.kt` | Background entrypoint used by supported environments. |
| `DeviceStatusReader.kt` | Reads battery and performance data with safe fallbacks. |
| `AutoHookControllerEngine.kt` | Safety controller for background behavior. |
| `RomAdaptiveEngine.kt` | Device profile helper for ColorOS, OxygenOS and Realme UI. |
| `auto-release-apk.yml` | Automatic build and GitHub Release workflow. |
| `manual-release-apk.yml` | Manual APK artifact workflow. |

---

## Troubleshooting

### Battery-Rock keeps stopping

Install v1.0.8 or newer. The startup path was changed so the launcher opens through `BatteryRockStatus.kt`.

### GitHub Actions build fails

Open the failed workflow step and check the first real Gradle or Kotlin error above the final `exit code 1` line. The last line is usually just the funeral notice, not the cause.

### App opens but status shows inactive

The dashboard can still open normally while inactive. Check your app setup and install the latest release build.

---

## Safety note

Battery-Rock is for testing on devices you own and control. Device behavior depends on ROM version, installed packages and system settings. Test carefully before using it daily.
