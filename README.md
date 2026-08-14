<div align="center">

# Battery-Rock

### An Xposed Module for advanced battery health, RAM/ROM status, and performance insights

**Battery status · RAM pressure · ROM storage · Phone performance level · Premium dark UI**

[![Battery-Rock](https://img.shields.io/badge/Battery--Rock-v1.1.0-818CF8?style=for-the-badge&logo=android&logoColor=white)](https://github.com/SayanthRock/Battery-Rock)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-12%2B-22C55E?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Releases](https://img.shields.io/badge/GitHub-Releases-111827?style=for-the-badge&logo=github)](https://github.com/SayanthRock/Battery-Rock/releases)
[![Xposed](https://img.shields.io/badge/Xposed-Module-E91E63?style=for-the-badge&logo=xda-developers&logoColor=white)](#)

Battery-Rock is a powerful Android Xposed Module designed to provide deep system insights and optimize device performance, specifically tailored for OPPO, Realme, and OnePlus devices. It features a comprehensive dashboard for monitoring battery health, charging status, temperature, estimated capacity, RAM pressure, internal ROM storage, and overall phone performance levels.

</div>

---

## ⚠️ Prerequisites

Before installing Battery-Rock, ensure your device meets the following requirements:

- **Root Access:** Your device must be rooted (e.g., via Magisk or KernelSU).
- **Xposed Framework:** An Xposed-compatible framework must be installed and active (LSPosed is highly recommended).
- **Supported OS:** Android 12 or higher (API 31+).

---

## ⚡ Features

### 🔋 Battery & System Dashboard
- **Battery Analytics:** View live battery percentage, charging state, health, temperature, voltage, and power source.
- **Capacity Estimate:** Displays estimated battery capacity when the Android system exposes sufficient data.
- **RAM Pressure Monitoring:** Track available RAM, total RAM, and estimated RAM load in real-time.
- **ROM Storage Insights:** Monitor internal storage free space, total size, and usage percentage.
- **Performance Level:** Calculates a comprehensive phone performance score based on CPU, Android API, RAM, ROM, and memory class.
- **Recommended Tuning Profile:** Suggests Safe, Balanced, Smooth, or Performance profiles based on live device telemetry.
- **Safe Fallbacks:** Intelligently handles hidden memory, storage, or system properties by displaying `Unknown` rather than causing app crashes.
- **Widgets:** Includes a variety of customizable home screen widgets (Minimal, Details, Dashboard, Health, Monitor) for quick access to stats.

### 🛠 Xposed Capabilities
As an Xposed module, Battery-Rock hooks into the system framework to provide advanced capabilities beyond standard applications:
- **Telemetry Control:** Hooks to manage and optimize background system telemetry.
- **Wakelock Management:** Guards against excessive wakelocks to improve standby battery life.
- **ROM Adaptive Engine:** Dynamically adjusts behaviors based on the specific ROM (ColorOS, OxygenOS, RealmeUI) detected.

---

## 📥 Installation

1. Download the latest APK from the [Releases page](https://github.com/SayanthRock/Battery-Rock/releases).
   - *Recommended file: `Battery-Rock-v1.1.0.apk`*
2. Install the APK on your device.
3. Open your Xposed manager (e.g., LSPosed).
4. Enable the **Battery-Rock** module.
5. Select the recommended system frameworks/apps in the module scope (if not automatically selected).
6. **Reboot your device** for the hooks to apply.

---

## 🚀 Latest Update: v1.1.0

This update significantly improves **RAM, ROM, and performance level detection**, providing clearer insights into device capabilities, storage pressure, and recommended tuning profiles.

| Area | Status |
|---|---|
| APK version | `1.1.0` |
| Version code | `11` |
| Minimum Android | Android 12 / API 31 |
| Target SDK | Android 15 / API 35 |
| UI style | Premium dark glass dashboard |

**What's New in v1.1.0:**
- Added real RAM diagnostics (Total, Available, Load %).
- Added internal ROM storage diagnostics (Total, Free, Used %).
- Improved performance scoring algorithm.
- Added recommended profile cards (Safe, Balanced, Smooth, Performance).
- Added RAM/ROM smart-control UI section.
- Safer fallback values for restrictive ROMs.

---

## 🛠 Building from Source

### Requirements
- JDK 17
- Android SDK 35
- Gradle 8.9
- Kotlin 2.0.21

### Local Build
To build a release APK locally, run:
```bash
gradle :app:assembleRelease
```
The output APK will be located at: `app/build/outputs/apk/release/`

### Automated Workflows
This repository utilizes GitHub Actions for automated builds:
- `.github/workflows/auto-release-apk.yml`: Automatically builds and publishes a GitHub Release when code is pushed to `main` or a tag is created.
- `.github/workflows/manual-release-apk.yml`: Allows manual triggering to build an APK and upload it as a workflow artifact.

---

## 🛟 Troubleshooting

- **Battery-Rock keeps stopping:** Ensure you are running v1.0.8 or newer (v1.1.0 recommended), which includes startup fixes.
- **RAM or ROM status shows Unknown:** Some aggressive ROM builds hide memory or storage properties. Battery-Rock is designed to show fallback values instead of crashing in these scenarios.
- **Module not working:** Double-check that the module is enabled in your Xposed manager and that you have rebooted your device.
- **App opens but status shows inactive:** The dashboard can function passively. Ensure you have the latest release and that Xposed is actively hooking the system.

---

## ⚠️ Safety Note & Disclaimer

Battery-Rock interacts deeply with your system framework. It is designed for testing and use on devices you own and control. Device behavior can vary significantly depending on your specific ROM version, installed packages, and system settings.

**Use at your own risk.** Test carefully before relying on it for daily use. The developers are not responsible for any bootloops, data loss, or device instability.
