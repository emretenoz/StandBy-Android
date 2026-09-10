package com.example.standby.data.settings

data class StandBySettings(
    val automaticModeEnabled: Boolean = true,
    val use24HourClock: Boolean = true,
    val showSeconds: Boolean = false,
    val screenTimeout: ScreenTimeout = ScreenTimeout.SYSTEM_DEFAULT,
    val keepScreenAwake: Boolean = true,
    val leftWidgets: List<StandByWidgetType> = listOf(
        StandByWidgetType.CLOCK,
        StandByWidgetType.BATTERY,
    ),
    val rightWidgets: List<StandByWidgetType> = listOf(
        StandByWidgetType.DATE,
        StandByWidgetType.CHARGING,
    ),
)

enum class StandByWidgetType(val label: String) {
    CLOCK("Clock"),
    DATE("Date"),
    BATTERY("Battery"),
    CHARGING("Charging status"),
}

enum class WidgetColumn { LEFT, RIGHT }

enum class ScreenTimeout(val label: String) {
    SYSTEM_DEFAULT("Follow system"),
    ONE_MINUTE("Keep awake 1 minute"),
    FIVE_MINUTES("Keep awake 5 minutes"),
    NEVER("Never while active"),
}
