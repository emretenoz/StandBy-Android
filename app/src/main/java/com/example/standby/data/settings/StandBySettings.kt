package com.example.standby.data.settings

data class StandBySettings(
    val automaticModeEnabled: Boolean = true,
    val use24HourClock: Boolean = true,
    val showSeconds: Boolean = false,
    val screenTimeout: ScreenTimeout = ScreenTimeout.SYSTEM_DEFAULT,
    val keepScreenAwake: Boolean = true,
    val clockFace: ClockFace = ClockFace.DIGITAL,
    val clockColor: ClockColor = ClockColor.INHERIT,
    val showDate: Boolean = true,
    val nightMode: Boolean = false,
    val burnInProtection: Boolean = true,
    val dayTheme: StandByThemeId = StandByThemeId.CLASSIC,
    val nightTheme: StandByThemeId = StandByThemeId.CRIMSON_NIGHT,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.SOLID,
    val typographyStyle: TypographyStyle = TypographyStyle.AUTO,
    val customBackground: Long = 0xFF000000,
    val customPrimary: Long = 0xFFF1EDE3,
    val customSecondary: Long = 0xFF9D9990,
    val customAccent: Long = 0xFFFF9F0A,
    val clockWidgetStyle: ClockWidgetStyle = ClockWidgetStyle.MINIMAL,
    val dateWidgetStyle: DateWidgetStyle = DateWidgetStyle.EDITORIAL,
    val batteryWidgetStyle: BatteryWidgetStyle = BatteryWidgetStyle.CIRCULAR,
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
    INHERIT("Theme color", 0x00000000),
    WHITE("White", 0xFFF7F7F4),
    WARM_WHITE("Warm white", 0xFFF1EDE3),
    RED("Red", 0xFFFF453A),
    ORANGE("Orange", 0xFFFF9F0A),
    BLUE("Blue", 0xFF64D2FF),
    GREEN("Green", 0xFF66D17A),
    PURPLE("Purple", 0xFFBF8CFF),
}

enum class StandByThemeId(val label: String) {
    CLASSIC("Classic"),
    MIDNIGHT("Midnight"),
    CRIMSON_NIGHT("Crimson Night"),
    SUNSET("Sunset"),
    FOREST("Forest"),
    NEON("Neon"),
    MONO_LIGHT("Mono Light"),
    CUSTOM("Custom"),
}

enum class BackgroundStyle(val label: String) {
    SOLID("Solid"),
    RADIAL("Radial shading"),
    VIGNETTE("Soft vignette"),
    AURA("Color aura"),
    GRAIN("Subtle grain"),
}

enum class TypographyStyle(val label: String) {
    AUTO("Theme & face"),
    MODERN("Modern"),
    EDITORIAL("Editorial"),
    ROUNDED("Rounded"),
    CONDENSED("Condensed"),
    MONO("Mono"),
}

enum class ClockWidgetStyle(val label: String) {
    MINIMAL("Minimal"), BOLD("Bold"), EDITORIAL("Editorial"), COMPACT("Compact")
}

enum class DateWidgetStyle(val label: String) {
    NUMERIC("Numeric"), EDITORIAL("Editorial"), CALENDAR("Calendar"), MINIMAL("Ultra-minimal")
}

enum class BatteryWidgetStyle(val label: String) {
    CIRCULAR("Circular"), HORIZONTAL("Horizontal"), PERCENTAGE("Large percentage"), MINIMAL("Minimal icon")
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
