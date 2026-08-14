package dev.sayanthrock.batteryrock.widget

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object WidgetConfigStore {
    private const val PREFS_NAME = "battery_rock_widget_configs"
    private const val KEY_PREFIX = "widget_config_"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveConfig(context: Context, appWidgetId: Int, config: WidgetConfig) {
        val json = gson.toJson(config)
        getPrefs(context).edit().putString("$KEY_PREFIX$appWidgetId", json).apply()
    }

    fun getConfig(context: Context, appWidgetId: Int): WidgetConfig {
        val json = getPrefs(context).getString("$KEY_PREFIX$appWidgetId", null)
        return if (json != null) {
            try {
                gson.fromJson(json, WidgetConfig::class.java)
            } catch (e: Exception) {
                WidgetConfig()
            }
        } else {
            WidgetConfig()
        }
    }

    fun deleteConfig(context: Context, appWidgetId: Int) {
        getPrefs(context).edit().remove("$KEY_PREFIX$appWidgetId").apply()
    }
}
