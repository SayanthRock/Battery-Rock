<div align="center">

# Battery-Rock

### Battery health and performance dashboard for OPPO, Realme and OnePlus

**Battery status · Phone performance level · Premium dark UI · APK release workflows**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.0.9-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Releases](https://img.shields.io/badge/GitHub-Releases-111827?style=for-the-badge&logo=github)](https://github.com/SayanthRock/Battery-Rock/releases)

Battery-Rock is an Android app dashboard for battery health, charging status, temperature, estimated capacity, phone performance level and device status guidance.

</div>

---

## Latest update: v1.0.9

This update refreshes the **UI, UX, design style and theme** with a cleaner premium dark interface.

| Area | Status |
|---|---|
| APK version | `1.0.9` |
| Version code | `10` |
| Package | `dev.sayanthrock.batteryrock` |
| Minimum Android | Android 12 / API 31 |
| Target SDK | Android 15 / API 35 |
| Kotlin | `2.0.21` |
| AGP | `8.7.3` |
| Java toolchain | `17` |
| UI style | Premium dark glass dashboard |
| Automatic release workflow | Enabled |
| Manual APK artifact workflow | Available |

### What changed in v1.0.9

- Redesigned the main dashboard with a premium hero card.
- Added quick status cards for battery, health and performance score.
- Improved spacing, card radius, contrast and typography hierarchy.
- Refreshed the dark theme palette with indigo, cyan, green and amber accents.
- Updated XML colors to match the Compose theme.
- Bumped the APK to `1.0.9` / versionCode `10`.

---

## Features

| Feature | Description |
|---|---|
| Battery dashboard | Shows battery percentage, charging state, health, temperature, voltage and power source. |
| Capacity estimate | Shows estimated battery capacity when Android exposes enough data. |
| Performance level | Calculates a simple phone performance level from CPU cores, Android API and memory class. |
| Safe fallbacks | Shows `Unknown` instead of closing when a device hides battery or performance details. |
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
Battery-Rock-v1.0.9.apk
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
- a tag like `v1.0.9` is pushed
- the workflow is started manually from GitHub Actions

For normal pushes to `main`, the workflow creates a build tag like:

```text
v1.0.9-build.123
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
| `MainActivity.kt` | Premium Compose dashboard UI. |
| `BatteryRockStatus.kt` | Safe status bridge for normal app startup. |
| `BatteryRockInit.kt` | Runtime entrypoint used by supported environments. |
| `DeviceStatusReader.kt` | Reads battery and performance data with safe fallbacks. |
| `ui/theme/Theme.kt` | Material color scheme and system bar styling. |
| `auto-release-apk.yml` | Automatic build and GitHub Release workflow. |
| `manual-release-apk.yml` | Manual APK artifact workflow. |

---

## Troubleshooting

### Battery-Rock keeps stopping

Install v1.0.8 or newer. v1.0.9 keeps that startup fix and adds the improved UI.

### GitHub Actions build fails

Open the failed workflow step and check the first real Gradle or Kotlin error above the final `exit code 1` line. The last line is usually just the funeral notice, not the cause.

### App opens but status shows inactive

The dashboard can still open normally while inactive. Install the latest release build first.

---

## Safety note

Battery-Rock is for testing on devices you own and control. Device behavior depends on ROM version, installed packages and system settings. Test carefully before using it daily.
