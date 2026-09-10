package com.example.standby.data.settings

data class StandBySettings(
    val automaticModeEnabled: Boolean = true,
    val use24HourClock: Boolean = true,
    val showSeconds: Boolean = false,
    val screenTimeout: ScreenTimeout = ScreenTimeout.SYSTEM_DEFAULT,
    val keepScreenAwake: Boolean = true,
    val clockFace: ClockFace = ClockFace.DIGITAL,
    val clockColor: ClockColor = ClockColor.WARM_WHITE,
    val showDate: Boolean = true,
    val nightMode: Boolean = false,
    val burnInProtection: Boolean = true,
    val leftWidgets: List<StandByWidgetType> = listOf(
        StandByWidgetType.CLOCK,
        StandByWidgetType.BATTERY,
    ),
    val rightWidgets: List<StandByWidgetType> = listOf(
        StandByWidgetType.DATE,
        StandByWidgetType.CHARGING,
    ),
)

enum class ClockFace(val label: String) {
    DIGITAL("Digital"),
    DIGITAL_SPLIT("Digital Split"),
    ANALOG("Analog"),
    ORBIT("Solar / Orbit"),
    WORLD("World Clock"),
}

enum class ClockColor(val label: String, val argb: Long) {
    WHITE("White", 0xFFF7F7F4),
    WARM_WHITE("Warm white", 0xFFF1EDE3),
    RED("Red", 0xFFFF453A),
    ORANGE("Orange", 0xFFFF9F0A),
    BLUE("Blue", 0xFF64D2FF),
    GREEN("Green", 0xFF66D17A),
    PURPLE("Purple", 0xFFBF8CFF),
}

enum class StandByWidgetType(val label: String) {
    CLOCK("Clock"),
    DATE("Date"),
    BATTERY("Battery"),
    CHARGING("Charging status"),
    WORLD_CLOCK("World clock"),
}

enum class WidgetColumn { LEFT, RIGHT }

enum class ScreenTimeout(val label: String) {
    SYSTEM_DEFAULT("Follow system"),
    ONE_MINUTE("Keep awake 1 minute"),
    FIVE_MINUTES("Keep awake 5 minutes"),
    NEVER("Never while active"),
}
