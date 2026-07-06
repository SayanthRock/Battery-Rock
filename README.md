<div align="center">

# Battery-Rock

### Battery health, RAM/ROM status and performance dashboard for OPPO, Realme and OnePlus

**Battery status · RAM pressure · ROM storage · Phone performance level · Premium dark UI · APK release workflows**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.1.0-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Releases](https://img.shields.io/badge/GitHub-Releases-111827?style=for-the-badge&logo=github)](https://github.com/SayanthRock/Battery-Rock/releases)

Battery-Rock is an Android app dashboard for battery health, charging status, temperature, estimated capacity, RAM pressure, internal ROM storage, phone performance level and device status guidance.

</div>

---

## Latest update: v1.1.0

This update improves **RAM, ROM and performance level detection** so the dashboard can show clearer device capability, storage pressure and recommended tuning profiles.

| Area | Status |
|---|---|
| APK version | `1.1.0` |
| Version code | `11` |
| Package | `dev.sayanthrock.batteryrock` |
| Minimum Android | Android 12 / API 31 |
| Target SDK | Android 15 / API 35 |
| Kotlin | `2.0.21` |
| AGP | `8.7.3` |
| Java toolchain | `17` |
| UI style | Premium dark glass dashboard |
| Automatic release workflow | Enabled |
| Manual APK artifact workflow | Available |

### What changed in v1.1.0

- Added real RAM diagnostics with total RAM, available RAM and RAM load percentage.
- Added internal ROM storage diagnostics with total storage, free storage and used percentage.
- Improved the performance score to include CPU cores, Android API, RAM, storage headroom and app memory class.
- Added a recommended profile card for Safe, Balanced, Smooth or Performance tuning.
- Added a RAM/ROM smart-control section in the dashboard UI.
- Added safer fallback values when a ROM hides memory, storage or system-property details.
- Bumped the APK to `1.1.0` / versionCode `11`.

---

## Features

| Feature | Description |
|---|---|
| Battery dashboard | Shows battery percentage, charging state, health, temperature, voltage and power source. |
| Capacity estimate | Shows estimated battery capacity when Android exposes enough data. |
| RAM pressure | Shows available RAM, total RAM and estimated RAM load. |
| ROM storage | Shows internal storage free space, total size and used percentage. |
| Performance level | Calculates a stronger phone performance level from CPU, Android API, RAM, ROM and memory class. |
| Recommended profile | Suggests Safe, Balanced, Smooth or Performance profile based on live device status. |
| Safe fallbacks | Shows `Unknown` instead of closing when a device hides battery, RAM, ROM or performance details. |
| Premium dark UI | Gradient background, glass cards, compact quick stats and clear sections. |
| Release automation | Builds APKs and publishes GitHub Releases automatically. |

---

## Download

Open the releases page and download the latest APK:

```text
https://github.com/SayanthRock/Battery-Rock/releases
```

Recommended APK file name:

```text
Battery-Rock-v1.1.0.apk
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
- a tag like `v1.1.0` is pushed
- the workflow is started manually from GitHub Actions

For normal pushes to `main`, the workflow creates a build tag like:

```text
v1.1.0-build.123
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
| `MainActivity.kt` | Premium Compose dashboard UI for battery, RAM, ROM and performance status. |
| `BatteryRockStatus.kt` | Safe status bridge for normal app startup. |
| `BatteryRockInit.kt` | Runtime entrypoint used by supported environments. |
| `DeviceStatusReader.kt` | Reads battery, RAM, ROM and performance data with safe fallbacks. |
| `ui/theme/Theme.kt` | Material color scheme and system bar styling. |
| `auto-release-apk.yml` | Automatic build and GitHub Release workflow. |
| `manual-release-apk.yml` | Manual APK artifact workflow. |

---

## Troubleshooting

### Battery-Rock keeps stopping

Install v1.0.8 or newer. v1.1.0 keeps that startup fix and adds RAM/ROM performance diagnostics.

### RAM or ROM status shows Unknown

Some ROM builds hide memory, storage or system-property values. Battery-Rock will keep the dashboard open and show fallback values instead of crashing.

### GitHub Actions build fails

Open the failed workflow step and check the first real Gradle or Kotlin error above the final `exit code 1` line. The last line is usually just the funeral notice, not the cause.

### App opens but status shows inactive

The dashboard can still open normally while inactive. Install the latest release build first.

---

## Safety note

Battery-Rock is for testing on devices you own and control. Device behavior depends on ROM version, installed packages and system settings. Test carefully before using it daily.
