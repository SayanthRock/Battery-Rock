<div align="center">

# Battery-Rock

### OPPO · Realme · OnePlus Battery Backup Improvement Module

**Battery Backup · Battery Health · Phone Performance Level · Automatic GitHub Release APK**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.0.6-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)

Battery-Rock is an Android module-style app for OPPO, Realme, and OnePlus devices. It includes a dark Compose dashboard for battery status, battery health, phone performance level, and supported package scope guidance.

</div>

---

## Latest Update, v1.0.6

This update restores automatic GitHub Release publishing with a dedicated APK release workflow. A manual artifact workflow is still available when you only want to build and download the APK without publishing a release.

| Area | Status |
|---|---|
| APK version | `1.0.6` |
| Version code | `7` |
| Android target | Android 12+ / SDK 35 |
| Kotlin | `2.0.21` |
| AGP | `8.7.3` |
| Automatic GitHub Release workflow | Added |
| Manual APK artifact workflow | Available |
| GitHub Release upload | Automatic through workflow |

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
| Automatic release | Builds APK, uploads artifact, and publishes GitHub Release automatically |
| Manual artifact build | Builds APK artifact without publishing a release |

---

## Automatic GitHub Release Workflow

Workflow file:

```text
.github/workflows/auto-release-apk.yml
```

This workflow builds the release APK and publishes a GitHub Release automatically.

It runs when:

- code is pushed to `main`
- a tag like `v1.0.6` is pushed
- it is started manually from GitHub Actions

For a normal push to `main`, it creates a build tag like:

```text
v1.0.6-build.123
```

The release includes:

- release APK
- `SHA256SUMS.txt`
- `APK_INFO.txt`
- generated release notes

---

## Manual APK Artifact Workflow

Workflow file:

```text
.github/workflows/manual-release-apk.yml
```

This workflow only builds and uploads the APK as a workflow artifact. It does not publish a GitHub Release.

Use it when you want to download the APK first and upload manually later.

---

## Normal Local APK Build

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
├── .github/workflows/auto-release-apk.yml
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
| `auto-release-apk.yml` | Automatic APK build and GitHub Release publishing workflow |
| `manual-release-apk.yml` | Manual APK artifact workflow without release publishing |

---

## Safety Note

Battery-Rock is for advanced Android users testing on devices they own and control. Use recommended scopes carefully and test each ROM/device setup before relying on it daily.
