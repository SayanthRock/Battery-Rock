package dev.sayanthrock.batteryrock.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.sayanthrock.batteryrock.ui.theme.BatteryRockTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val initialConfig = WidgetConfigStore.getConfig(this, appWidgetId)

        setContent {
            BatteryRockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetDesignerScreen(
                        initialConfig = initialConfig,
                        onSave = { newConfig ->
                            WidgetConfigStore.saveConfig(this, appWidgetId, newConfig)
                            completeConfiguration()
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun completeConfiguration() {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        // Force update the widget
        CoroutineScope(Dispatchers.IO).launch {
            WidgetUpdater.updateAllWidgets(this@WidgetConfigActivity)
        }

        setResult(RESULT_OK, resultValue)
        finish()
    }
}
