<div align="center">

# Battery-Rock

### OPPO · Realme · OnePlus Battery Backup Improvement Module

**Battery Backup · Battery Health · Phone Performance Level · Automated APK Release**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.0.6-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)

Battery-Rock is an Android module-style app for OPPO, Realme, and OnePlus devices. It includes a dark Compose dashboard for battery status, battery health, phone performance level, and supported package scope guidance.

</div>

---

## Latest Update, v1.0.6

This update focuses on release APK build stability and clearer GitHub Actions diagnostics.

| Area | Status |
|---|---|
| APK version | `1.0.6` |
| Version code | `7` |
| Android target | Android 12+ / SDK 35 |
| Kotlin | `2.0.21` |
| AGP | `8.7.3` |
| Gradle in CI | `8.9` |
| Java in CI | `17` |
| Release workflow | Automated |
| APK artifact upload | Enabled |
| GitHub Release upload | Enabled |
| Diagnostics artifact | Enabled |
| Important error extraction | Enabled |

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
| GitHub Actions | Builds APK, uploads artifacts, creates release notes, and publishes GitHub Release automatically |
| Diagnostics | Stores full Gradle logs and extracts real error lines when CI fails |

---

## Automated APK Build

Main workflow file:

```text
.github/workflows/build-release.yml
```

The workflow runs when:

- Code is pushed to the `main` branch
- A version tag like `v1.0.6` is pushed
- The workflow is started manually from GitHub Actions

For normal pushes to `main`, the workflow automatically creates a tag like:

```text
v1.0.6-build.123
```

The workflow prepares and uploads:

- Release APK
- `SHA256SUMS.txt`
- `APK_INFO.txt`
- Automatic release notes from recent commits
- APK workflow artifact
- Diagnostics artifact
- Public GitHub Release with APK files attached

---

## Build Troubleshooting

If GitHub Actions fails in **Build release APK**, open that step and look for:

```text
Important Gradle errors
```

The workflow now saves the full build log here:

```text
diagnostics/build-release-apk.log
```

It also extracts likely useful failure lines into:

```text
diagnostics/important-errors.txt
```

Avoid using **Re-run** on an old failed run after code changes. Open the newest run created by the newest commit, otherwise GitHub rebuilds the old broken source. Yes, buttons are apparently allowed to be this misleading.

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
├── .github/workflows/build-release.yml
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
| `build-release.yml` | Automatic APK build, diagnostics, artifact upload, and release publishing |

---

## Local Build

For local Android Studio or terminal builds, place the compile-only API jar here:

```text
app/libs/api-82.jar
```

Build with:

```bash
gradle :app:assembleRelease
```

GitHub Actions prepares the compile-only API dependency automatically for CI builds.

---

## Safety Note

Battery-Rock is for advanced Android users testing on devices they own and control. Use recommended scopes carefully and test each ROM/device setup before relying on it daily.
