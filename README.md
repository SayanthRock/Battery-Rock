<div align="center">

# 🔋 Battery-Rock

### OPPO · Realme · OnePlus Battery Backup Improvement LSPosed Module

**Battery Backup · Battery Health · Phone Performance Level · GitHub APK Release Automation**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.0.4-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![LSPosed](https://img.shields.io/badge/LSPosed-Module-4F46E5?style=for-the-badge&logo=android)](https://github.com/LSPosed/LSPosed)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)

**Battery-Rock is a SayanthRock LSPosed module for OPPO, Realme, and OnePlus devices. It targets common background drain patterns and includes a live battery health and phone performance dashboard.**

</div>

---

## ✅ Latest Update, v1.0.4

This update fixes APK compatibility and keeps the public GitHub Release flow active.

| Area | Status |
|---|---|
| Battery backup improvement | Enabled |
| Battery health dashboard | Added |
| Phone performance level | Added |
| APK compatibility fix | Added |
| Android 12/13 battery source safety | Added |
| APK version | `1.0.4` |
| Version code | `5` |
| Repository visibility | Public |
| APK build workflow | Fully automated |
| GitHub Release upload | Enabled |
| SHA256 checksum generation | Enabled |
| APK metadata file | Enabled |
| APK signature verification | Enabled in CI |
| Diagnostics artifact | Enabled |

---

## 📌 What Battery-Rock Does

Battery-Rock is built for rooted users who use LSPosed on ColorOS, OxygenOS, or Realme UI devices.

It targets common battery-drain sources such as:

- Repeated background jobs
- Frequent telemetry alarms
- Long or indefinite wakelocks
- Selected telemetry network calls
- Selected analytics provider writes
- Unnecessary service activity inside scoped packages

The app dashboard shows:

- Battery percentage
- Charging state
- Battery health
- Battery temperature
- Voltage
- Power source
- Estimated capacity when Android exposes the data
- Phone performance level
- Android version and app memory class

Battery behavior depends on ROM version, installed apps, signal strength, kernel behavior, and LSPosed scope selection. Test on your own device before daily use.

---

## ✨ Main Features

| Area | Battery-Rock Action |
|---|---|
| LSPosed module | Java-layer hooks, no direct `/system` modification |
| Battery backup | Reduces selected background work, wakeups, and drain patterns |
| Battery health | Shows Android battery health, level, temperature, power source, and capacity estimate |
| Phone performance level | Calculates a clear status from CPU cores, Android API, low-RAM state, and memory class |
| JobScheduler | Blocks or reduces selected background job scheduling |
| AlarmManager | Throttles frequent telemetry alarms |
| Wakelocks | Caps oversized or indefinite wakelock requests |
| Telemetry packages | Reduces selected service, network, and analytics behavior |
| Compose UI | Dark, clean module dashboard with battery and performance cards |
| GitHub Actions | Builds APK, creates release notes, uploads artifact, and publishes GitHub Release automatically |

---

## 🚀 Fully Automated APK Build and Public Upload

The main workflow file is:

```text
.github/workflows/build-release.yml
```

The workflow runs when:

- Code is pushed to the `main` branch
- A version tag like `v1.0.4` is pushed
- The workflow is started manually from GitHub Actions

For normal pushes to `main`, the workflow automatically creates a tag like:

```text
v1.0.4-build.123
```

The workflow reads the app version directly from:

```text
app/build.gradle.kts
```

The workflow prepares and uploads:

- Release APK
- `SHA256SUMS.txt`
- `APK_INFO.txt`
- Automatic release notes from recent commits
- GitHub Actions APK artifact
- GitHub Actions diagnostics artifact
- Public GitHub Release with APK files attached

---

## 🧱 Project Structure

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
│       │   │   ├── FrameworkHook.kt
│       │   │   ├── TelemetryKiller.kt
│       │   │   └── WakelockGuard.kt
│       │   └── ui/theme/Theme.kt
│       └── res/xml/scope.xml
├── .github/workflows/build-release.yml
├── CHANGELOG.md
├── README.md
└── .gitignore
```

---

## 🧠 Core Files

| File | Purpose |
|---|---|
| `BatteryRockInit.kt` | LSPosed entry point loaded from `assets/xposed_init` |
| `DeviceStatusReader.kt` | Reads safe Android battery health and device performance status for the dashboard |
| `MainActivity.kt` | Compose UI with module status, battery health, performance level, and package list |
| `FrameworkHook.kt` | Framework-level hooks for JobScheduler and AlarmManager behavior |
| `TelemetryKiller.kt` | Hooks selected telemetry services, jobs, network calls, and analytics writes |
| `WakelockGuard.kt` | Caps long or indefinite wakelocks |
| `AndroidManifest.xml` | LSPosed module metadata and launcher activity |
| `scope.xml` | Recommended LSPosed scope packages |
| `build-release.yml` | Automatic APK build, artifact upload, release notes, and GitHub Release publishing |

---

## 🎯 Recommended LSPosed Scope

| Package | Purpose |
|---|---|
| `android` | Android framework and system_server hooks |
| `com.android.systemui` | SystemUI wakelock control |
| `com.oplus.onetrace` | OPLUS trace and telemetry |
| `com.oplus.appsense` | Usage analytics |
| `com.oplus.powermonitor` | Power monitor behavior |
| `com.oplus.logkit` | Log collection |
| `com.oplus.olc` | OPLUS log center |
| `com.debug.loggerui` | MTK logger control |
| `com.oplus.sau` | System app updater |
| `com.oplus.romupdate` | ROM update service |
| `com.nearme.instant.platform` | OPPO instant apps platform |
| `com.oplus.appplatform` | OPLUS app platform |
| `com.oplus.ocrservice` | OPLUS OCR service |
| `com.coloros.ocrservice` | ColorOS OCR service |
| `com.realme.systemservice` | Realme system service |
| `com.realme.statisticsservice` | Realme statistics service |
| `com.oneplus.statistics` | OnePlus statistics collection |

---

## ⚙️ Local Build

For local Android Studio or terminal builds, place the Xposed API jar here:

```text
app/libs/api-82.jar
```

If a Gradle wrapper exists, build with:

```bash
./gradlew :app:assembleRelease
```

If this repository has no Gradle wrapper in your checkout, install Gradle and build with:

```bash
gradle :app:assembleRelease
```

GitHub Actions prepares the Xposed API dependency automatically for CI builds.

---

## 📲 Installation

1. Download the latest APK from GitHub Releases.
2. Install the APK on your rooted Android device.
3. Open LSPosed Manager.
4. Enable Battery-Rock.
5. Select the recommended scope packages.
6. Reboot the phone.
7. Open Battery-Rock and check the module status.
8. Review LSPosed logs to confirm hooks are loading.

---

## ✅ Verification Checklist

After reboot, check:

- Battery-Rock appears enabled in LSPosed.
- The module app opens without crashing.
- The module status card is visible.
- Battery Health card shows live status.
- Phone Performance Level card shows a calculated level.
- LSPosed logs show Battery-Rock hook messages.
- Calls, messages, Wi-Fi, Bluetooth, charging, and notifications still work.
- Idle drain is compared before and after at least one sleep-cycle test.

---

## 🧪 Testing Commands

Useful commands for comparing behavior before and after enabling the module:

```bash
adb shell dumpsys batterystats
adb shell dumpsys alarm
adb shell dumpsys jobscheduler
adb shell dumpsys power
```

Recommended test method:

1. Fully charge the phone.
2. Test idle drain overnight with Battery-Rock disabled.
3. Enable Battery-Rock and reboot.
4. Test idle drain overnight again under similar conditions.
5. Compare battery percentage drop, alarms, jobs, and wakelocks.

---

## 🛡️ Safety Notes

Battery-Rock is a root and LSPosed module. Use it only on devices you own and control.

- It does not modify `/system` directly.
- It can be disabled from LSPosed Manager.
- A reboot restores normal behavior after disabling the module.
- ROM behavior can vary by brand, Android version, and firmware build.
- If any system feature breaks, remove that package from scope or disable the module.

---

## 🧩 Troubleshooting

| Problem | Fix |
|---|---|
| GitHub Actions fails before build | Check Gradle files and app module files exist at repository root |
| Build fails because `api-82.jar` is missing locally | Add `api-82.jar` to `app/libs/` or use GitHub Actions |
| No APK is found after build | Check `app/build/outputs/apk/release/` and workflow diagnostics |
| APK signature check fails | Rebuild from GitHub Actions and inspect diagnostics artifact |
| Module does not activate | Enable the module in LSPosed, select scope, then reboot |
| Hooks do not appear in logs | Confirm scope packages exist on your ROM |
| Battery Health shows unknown values | Some ROMs hide capacity or voltage values from normal apps |
| System feature breaks | Remove that package from scope, disable the module, then reboot |

---

## 🗺️ Next Improvements

- Add proper release keystore support through GitHub repository secrets.
- Add LSPosed log viewer screen.
- Add package scope status checker.
- Add ROM profile presets for ColorOS, OxygenOS, and Realme UI.
- Add stronger safe, balanced, and advanced mode behavior.
- Re-enable R8/minify after stable hook testing.

---

## 👤 Brand

**Battery-Rock** is part of the **SayanthRock** Android tools collection.

| Brand | Details |
|---|---|
| Developer | SayanthRock |
| Project | Battery-Rock |
| Type | Android LSPosed battery backup improvement module |
| UI Style | Dark, clean spacing, modern Android feel |
| Focus | OPPO, Realme, and OnePlus battery backup, battery health, and performance |

---

## 🙏 Credits

- **SayanthRock**, project development and branding
- **LSPosed**, Xposed module framework
- **XposedBridge**, API reference
- **Disable_Servers_Save_Battery**, Magisk-layer inspiration

---

<div align="center">

### 🔋 Battery-Rock by SayanthRock

**OPPO · Realme · OnePlus battery backup improvement through scoped LSPosed background control.**

[GitHub Profile](https://github.com/SayanthRock) · [Battery-Rock Repo](https://github.com/SayanthRock/Battery-Rock)

</div>
