package dev.sayanthrock.batteryrock.widget

enum class LayoutPreset {
    COMPACT, CLOCK_BATTERY, LARGE_CIRCULAR, WIDE, MINIMAL, CHARGING_DASHBOARD
}

enum class TapAction {
    OPEN_APP, OPEN_BATTERY_DETAILS, OPEN_SYSTEM_SETTINGS, OPEN_CHARGING_MONITOR, REFRESH
}

data class WidgetConfig(
    val preset: LayoutPreset = LayoutPreset.COMPACT,

    // Data Visibility
    val showPercentage: Boolean = true,
    val showTemperature: Boolean = true,
    val showChargingStatus: Boolean = true,
    val showChargingType: Boolean = false,
    val showChargingPower: Boolean = false,
    val showChargingCurrent: Boolean = false,
    val showChargingVoltage: Boolean = false,
    val showBatteryHealth: Boolean = false,
    val showBatteryTech: Boolean = false,
    val showBatteryCapacity: Boolean = false,
    val showEstChargingTime: Boolean = false,
    val showEstRemainingTime: Boolean = false,
    val showTime: Boolean = false,
    val showDate: Boolean = false,
    val showDayOfWeek: Boolean = false,
    val showBatteryCondition: Boolean = false,
    val showWarningStatus: Boolean = false,

    // Battery Ring Customization
    val ringEnabled: Boolean = true,
    val ringThicknessDp: Float = 6f,
    val ringSizeDp: Float = 64f,
    val ringStartAngle: Float = 270f, // -90 degrees, top
    val ringDirectionClockwise: Boolean = true,
    val ringRoundedCaps: Boolean = true,
    val percentageInsideRing: Boolean = false,
    val temperatureInsideRing: Boolean = false,

    // Visual Customization
    val backgroundColor: Long = 0xFF161925,
    val backgroundOpacity: Float = 1f,
    val useGlassEffect: Boolean = false,
    val borderWidthDp: Float = 1f,
    val cornerRadiusDp: Float = 24f,
    val showShadow: Boolean = false,

    // Colors
    val textColor: Long = 0xFFFFFFFF,
    val accentColor: Long = 0xFF8B5CF6,
    val ringBackgroundColor: Long = 0x33FFFFFF,
    val ringProgressColor: Long = 0xFF22C55E, // Default green
    val chargingColor: Long = 0xFF22C55E,
    val lowBatteryColor: Long = 0xFFEF4444,
    val warningColor: Long = 0xFFF59E0B,

    // Typography
    val clockSizeSp: Float = 32f,
    val percentageSizeSp: Float = 24f,
    val temperatureSizeSp: Float = 14f,
    val dateSizeSp: Float = 14f,
    val fontWeightBold: Boolean = true,

    // Alignment
    val horizontalAlignment: Int = 1, // 0=Start, 1=Center, 2=End
    val verticalAlignment: Int = 1, // 0=Top, 1=Center, 2=Bottom
    val internalPaddingDp: Float = 16f,
    val elementSpacingDp: Float = 8f,

    // Interaction
    val tapAction: TapAction = TapAction.OPEN_APP
)
