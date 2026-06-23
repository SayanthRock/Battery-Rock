# ── Battery-Rock ProGuard / R8 Rules ──────────────────────────────────────────

# Keep LSPosed module entry point declared in assets/xposed_init.
-keep class dev.sayanthrock.batteryrock.BatteryRockInit { *; }

# Keep all runtime hook classes used by LSPosed / Xposed reflection.
-keep class dev.sayanthrock.batteryrock.hooks.** { *; }

# Xposed API is compileOnly and provided by the LSPosed/Xposed environment.
-dontwarn de.robv.android.xposed.**
-keep class de.robv.android.xposed.** { *; }

# Optional runtime targets. Some ROMs or packages may not include these classes.
-dontwarn okhttp3.**
-dontwarn android.app.IAlarmListener
-dontwarn com.android.server.**

# Keep metadata and BuildConfig values used by the UI.
-keepattributes *Annotation*
-keep class dev.sayanthrock.batteryrock.BuildConfig { *; }
