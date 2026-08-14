package dev.sayanthrock.batteryrock.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    suspend fun updateAllWidgets(context: Context) {
        BatteryStatusWidget().updateAll(context)
        BatteryDetailsWidget().updateAll(context)
        ChargingMonitorWidget().updateAll(context)
        BatteryHealthWidget().updateAll(context)
        BatteryDashboardWidget().updateAll(context)
        MinimalBatteryWidget().updateAll(context)
        CustomizableBatteryWidget().updateAll(context)
    }
}
