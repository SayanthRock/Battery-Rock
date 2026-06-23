<div align="center">

# 🔋 Battery-Rock

### OPPO · Realme · OnePlus Battery Backup Improvement LSPosed Module

**ColorOS • OxygenOS • Realme UI • LSPosed • SayanthRock**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.0.0-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![LSPosed](https://img.shields.io/badge/LSPosed-Module-4F46E5?style=for-the-badge&logo=android)](https://github.com/LSPosed/LSPosed)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)

**Battery-Rock is a SayanthRock LSPosed module built to improve battery backup on OPPO, Realme, and OnePlus devices by reducing common background drain sources such as telemetry, excessive jobs, alarms, network calls, analytics writes, and wakelock abuse.**

Made with care by **SayanthRock**.

</div>

---

## 📌 What is Battery-Rock?

**Battery-Rock** is a complete **LSPosed module project** built for OPPO, Realme, and OnePlus devices.

Instead of freezing native daemons with shell commands, Battery-Rock works inside the Android Java layer using Xposed hooks. It targets known OPLUS telemetry and background service packages, then reduces common idle battery drain sources such as background jobs, alarms, network telemetry calls, analytics provider writes, and long wakelocks.

This project is designed for rooted users who understand LSPosed module scopes and want a clean, reversible, Java-level battery control module.

---

## 🎯 Project Goal

Battery-Rock is designed to solve **common battery backup problems** caused by unnecessary background activity on supported OPPO, Realme, and OnePlus firmware.

The module focuses on:

- Reducing idle drain
- Blocking selected telemetry behavior
- Limiting excessive jobs and alarms
- Capping long or indefinite wakelocks
- Keeping the setup reversible through LSPosed
- Improving background performance without modifying `/system`

Battery behavior depends on ROM version, installed apps, kernel behavior, signal strength, and user settings. Battery-Rock is built to fix the most common drain patterns, but every device should still be tested with logs and real idle-drain checks.

---

## ⚠️ Important Notice

Battery-Rock is a powerful root/LSPosed module. Use it only on devices you own and control.

- It does not modify `/system` directly.
- It can be disabled from LSPosed Manager.
- A reboot restores normal behavior after disabling the module.
- Device behavior can vary by ROM, Android version, and OEM firmware.
- Test carefully before using it as a daily driver.

This README describes the project structure and intended behavior. Real battery improvement must be verified on-device with logs and idle drain testing.

---

## ✨ Key Features

| Area | Battery-Rock Action |
|---|---|
| Service control | Stops selected telemetry services from doing background work |
| JobScheduler | Blocks or reduces selected background job scheduling |
| AlarmManager | Throttles frequent telemetry alarms |
| Network telemetry | Blocks selected outbound telemetry calls inside targeted packages |
| ContentProvider analytics | Drops selected analytics insert calls |
| Wakelock control | Caps indefinite wakelocks to safer time limits |
| LSPosed UI | Includes a simple Compose based app screen with module status and hook list |
| Reversible setup | Disable from LSPosed and reboot to restore stock behavior |

---

## 🧱 Project Structure

The uploaded Battery-Rock project contains **21 files** with a production-ready Android/LSPosed structure.

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
│       ├── assets/
│       │   └── xposed_init
│       ├── java/dev/sayanthrock/batteryrock/
│       │   ├── BatteryRockInit.kt
│       │   ├── MainActivity.kt
│       │   ├── hooks/
│       │   │   ├── FrameworkHook.kt
│       │   │   ├── TelemetryKiller.kt
│       │   │   └── WakelockGuard.kt
│       │   └── ui/theme/
│       │       └── Theme.kt
│       └── res/
│           ├── drawable/ic_launcher.xml
│           ├── values/colors.xml
│           ├── values/strings.xml
│           ├── values/themes.xml
│           └── xml/scope.xml
├── README.md
└── .gitignore
```

---

## 🧠 Core Files

| File | Purpose |
|---|---|
| `BatteryRockInit.kt` | LSPosed entry point loaded from `assets/xposed_init` |
| `hooks/FrameworkHook.kt` | Framework level hooks for `system_server`, including JobScheduler and AlarmManager logic |
| `hooks/TelemetryKiller.kt` | Blocks selected telemetry services, jobs, network calls, and analytics provider writes |
| `hooks/WakelockGuard.kt` | Caps indefinite or oversized wakelocks to a safer maximum duration |
| `MainActivity.kt` | Compose UI with glassmorphism style, active module badge, and hook list |
| `ui/theme/Theme.kt` | SayanthRock 2026 dark theme styling |
| `AndroidManifest.xml` | LSPosed module metadata, launcher activity, module description, and scope resource |
| `assets/xposed_init` | Points LSPosed to `dev.sayanthrock.batteryrock.BatteryRockInit` |
| `res/xml/scope.xml` | Recommended LSPosed scope list for OPLUS, Realme, OnePlus, Android framework, and SystemUI packages |

---

## 🎯 Recommended LSPosed Scope

The current `scope.xml` includes **17 scope entries**, including Android framework and SystemUI.

| Package | Purpose |
|---|---|
| `android` | Android framework, `system_server` hooks |
| `com.android.systemui` | SystemUI wakelock control |
| `com.oplus.onetrace` | OPLUS trace and telemetry |
| `com.oplus.appsense` | Usage and behavior analytics |
| `com.oplus.powermonitor` | Background power monitor |
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

## 🔁 How Battery-Rock Differs From the Magisk Reference

| | Disable_Servers_Save_Battery | Battery-Rock |
|---|---|---|
| Layer | Native shell and process control | Java and Xposed hooks |
| Method | Freezes binary daemons | Blocks Java methods in process |
| Root stack | Magisk, KernelSU, APatch | LSPosed with root environment |
| Best for | Native daemons such as `midasd` and `mobile_log_d` | Java telemetry APKs and framework calls |
| Revert method | Disable module or unfreeze daemons | Disable LSPosed module and reboot |
| Target style | Process level control | Method level control |

Battery-Rock is not a direct replacement for native daemon control. It is a Java-layer companion approach for telemetry and background behavior that happens inside Android framework and OEM APK processes.

---

## ⚙️ Hook Architecture

```text
LSPosed Manager
└── Battery-Rock module
    ├── android / system_server
    │   ├── JobScheduler hooks
    │   └── AlarmManager hooks
    ├── OPLUS / Realme / OnePlus telemetry packages
    │   ├── Service hooks
    │   ├── JobScheduler hooks
    │   ├── Network hooks
    │   └── ContentProvider hooks
    └── SystemUI and selected packages
        └── Wakelock guard hooks
```

---

## 📋 Requirements

- Rooted Android device
- LSPosed installed and working
- OPPO, Realme, or OnePlus firmware based on ColorOS, OxygenOS, or Realme UI
- Android 12 or newer recommended
- Android Studio or Gradle build environment
- Xposed API JAR for compile-time only dependency

---

## 🛠️ Build Setup

Before building, download `api-82.jar` from the official XposedBridge releases and place it here:

```text
app/libs/api-82.jar
```

Then build the release APK:

```bash
./gradlew assembleRelease
```

The APK will be generated here:

```text
app/build/outputs/apk/release/
```

---

## 📲 Installation

1. Build or download the Battery-Rock APK.
2. Install the APK on your rooted device.
3. Open **LSPosed Manager**.
4. Enable **Battery-Rock**.
5. Select the recommended scope packages from `scope.xml`.
6. Reboot the device.
7. Open Battery-Rock and check the active module badge.
8. Review LSPosed logs to confirm hooks are loading correctly.

---

## ✅ Verification Checklist

After reboot, check:

- Battery-Rock appears enabled inside LSPosed.
- The module app opens without crashing.
- The active badge is visible in the app UI.
- LSPosed logs show Battery-Rock hook messages.
- Normal calling, messaging, Wi-Fi, Bluetooth, charging, and notifications still work.
- Idle drain improves after at least one full sleep cycle test.

---

## 🧪 Testing Notes

For real battery testing, compare before and after results using:

```bash
adb shell dumpsys batterystats
adb shell dumpsys alarm
adb shell dumpsys jobscheduler
adb shell dumpsys power
```

Recommended test method:

1. Fully charge the phone.
2. Keep the same apps installed.
3. Test idle drain overnight with Battery-Rock disabled.
4. Enable Battery-Rock and reboot.
5. Test idle drain overnight again under similar conditions.
6. Compare wakelocks, alarms, jobs, and battery percentage drop.

---

## 🛡️ Safety Design

Battery-Rock is designed to be safer than hard-disabling random system packages.

- Uses scoped LSPosed hooks instead of deleting system apps.
- Avoids permanent system partition changes.
- Keeps behavior reversible.
- Uses package targeting instead of global blocking.
- Keeps framework level logic separated from app level hooks.
- Keeps the UI independent from hook execution.

Still, every ROM is different. If something breaks, disable the module in LSPosed and reboot.

---

## 🧩 Troubleshooting

| Problem | Fix |
|---|---|
| Build fails because `api-82.jar` is missing | Download `api-82.jar` and place it in `app/libs/` |
| Module does not activate | Check LSPosed is installed, enabled, and scope packages are selected |
| Hooks do not appear in logs | Reboot after enabling the module and verify the selected scope |
| App installs but no effect | Confirm the target package exists on your ROM |
| System feature breaks | Remove that package from scope, disable the module, then reboot |
| Release APK shrinks too much | Review `proguard-rules.pro` and keep Xposed hook classes |

---

## 🗺️ Roadmap

- Add GitHub Actions APK build workflow
- Add signed release build support
- Add release notes template
- Add LSPosed log viewer screen
- Add package scope status checker
- Add user selectable safe, balanced, and aggressive modes
- Add ROM profile presets for ColorOS, OxygenOS, and Realme UI
- Continue improving Battery-Rock to handle more common battery backup problems safely

---

## 👤 Brand

**Battery-Rock** is part of the **SayanthRock** Android tools collection.

| Brand | Details |
|---|---|
| Developer | SayanthRock |
| Project | Battery-Rock |
| Type | Android LSPosed battery backup improvement module |
| UI Style | Dark, glassmorphism, clean spacing, modern Android feel |
| Focus | OPPO, Realme, and OnePlus battery backup, idle drain control, cleaner background behavior |

---

## 🙏 Credits

- **SayanthRock**, project development and branding
- **LSPosed**, Xposed module framework
- **XposedBridge**, API reference
- **Disable_Servers_Save_Battery**, Magisk layer inspiration

---

<div align="center">

### 🔋 Battery-Rock by SayanthRock

**OPPO · Realme · OnePlus battery backup improvement through safer LSPosed background control.**

[GitHub Profile](https://github.com/SayanthRock) • [Battery-Rock Repo](https://github.com/SayanthRock/Battery-Rock)

</div>
