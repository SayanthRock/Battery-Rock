# ── Battery-Rock ProGuard Rules ───────────────────────────────────────────────

# Keep LSPosed module entry point (declared in assets/xposed_init)
-keep class dev.sayanthrock.batteryrock.BatteryRockInit { *; }

# Keep all hook objects (used via reflection by Xposed)
-keep class dev.sayanthrock.batteryrock.hooks.** { *; }

# Keep Xposed API interfaces
-keep class de.robv.android.xposed.** { *; }
-keepattributes *Annotation*

# Preserve BuildConfig for version display
-keep class dev.sayanthrock.batteryrock.BuildConfig { *; }
