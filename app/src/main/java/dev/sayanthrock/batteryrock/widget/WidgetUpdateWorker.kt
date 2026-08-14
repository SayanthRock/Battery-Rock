package dev.sayanthrock.batteryrock.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dev.sayanthrock.batteryrock.BatteryMonitorService

object WidgetUpdater {
    suspend fun updateAllWidgets(context: Context) {
        BatteryStatusWidget().updateAll(context)
        BatteryDetailsWidget().updateAll(context)
        ChargingMonitorWidget().updateAll(context)
        BatteryHealthWidget().updateAll(context)
        BatteryDashboardWidget().updateAll(context)
        MinimalBatteryWidget().updateAll(context)
    }
}
