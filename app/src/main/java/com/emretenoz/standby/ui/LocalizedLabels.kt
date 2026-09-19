package com.emretenoz.standby.ui

import android.content.Context
import androidx.annotation.StringRes
import com.emretenoz.standby.R
import com.emretenoz.standby.data.settings.BackgroundStyle
import com.emretenoz.standby.data.settings.BatteryWidgetStyle
import com.emretenoz.standby.data.settings.ClockColor
import com.emretenoz.standby.data.settings.ClockFace
import com.emretenoz.standby.data.settings.ClockWidgetStyle
import com.emretenoz.standby.data.settings.DateWidgetStyle
import com.emretenoz.standby.data.settings.ScreenTimeout
import com.emretenoz.standby.data.settings.StandByThemeId
import com.emretenoz.standby.data.settings.StandByWidgetType
import com.emretenoz.standby.data.settings.TypographyStyle
import com.emretenoz.standby.data.settings.WorldClockZone

@StringRes
fun ClockFace.labelRes(): Int = when (this) {
    ClockFace.DIGITAL -> R.string.clock_face_digital
    ClockFace.DIGITAL_SPLIT -> R.string.clock_face_digital_split
    ClockFace.ANALOG -> R.string.clock_face_analog
    ClockFace.ORBIT -> R.string.clock_face_orbit
    ClockFace.WORLD -> R.string.clock_face_world
}

@StringRes
fun ClockColor.labelRes(): Int = when (this) {
    ClockColor.INHERIT -> R.string.clock_color_theme
    ClockColor.WHITE -> R.string.color_white
    ClockColor.WARM_WHITE -> R.string.color_warm_white
    ClockColor.RED -> R.string.color_red
    ClockColor.ORANGE -> R.string.color_orange
    ClockColor.BLUE -> R.string.color_blue
    ClockColor.GREEN -> R.string.color_green
    ClockColor.PURPLE -> R.string.color_purple
}

@StringRes
fun StandByThemeId.labelRes(): Int = when (this) {
    StandByThemeId.CLASSIC -> R.string.theme_classic
    StandByThemeId.MIDNIGHT -> R.string.theme_midnight
    StandByThemeId.CRIMSON_NIGHT -> R.string.theme_crimson_night
    StandByThemeId.SUNSET -> R.string.theme_sunset
    StandByThemeId.FOREST -> R.string.theme_forest
    StandByThemeId.NEON -> R.string.theme_neon
    StandByThemeId.MONO_LIGHT -> R.string.theme_mono_light
    StandByThemeId.CUSTOM -> R.string.theme_custom
}

@StringRes
fun BackgroundStyle.labelRes(): Int = when (this) {
    BackgroundStyle.SOLID -> R.string.background_solid
    BackgroundStyle.RADIAL -> R.string.background_radial
    BackgroundStyle.VIGNETTE -> R.string.background_vignette
    BackgroundStyle.AURA -> R.string.background_aura
    BackgroundStyle.GRAIN -> R.string.background_grain
}

@StringRes
fun TypographyStyle.labelRes(): Int = when (this) {
    TypographyStyle.AUTO -> R.string.typography_auto
    TypographyStyle.MODERN -> R.string.typography_modern
    TypographyStyle.EDITORIAL -> R.string.typography_editorial
    TypographyStyle.ROUNDED -> R.string.typography_rounded
    TypographyStyle.CONDENSED -> R.string.typography_condensed
    TypographyStyle.MONO -> R.string.typography_mono
}

@StringRes
fun ClockWidgetStyle.labelRes(): Int = when (this) {
    ClockWidgetStyle.MINIMAL -> R.string.style_minimal
    ClockWidgetStyle.BOLD -> R.string.style_bold
    ClockWidgetStyle.EDITORIAL -> R.string.style_editorial
    ClockWidgetStyle.COMPACT -> R.string.style_compact
}

@StringRes
fun DateWidgetStyle.labelRes(): Int = when (this) {
    DateWidgetStyle.NUMERIC -> R.string.style_numeric
    DateWidgetStyle.EDITORIAL -> R.string.style_editorial
    DateWidgetStyle.CALENDAR -> R.string.style_calendar
    DateWidgetStyle.MINIMAL -> R.string.style_ultra_minimal
}

@StringRes
fun BatteryWidgetStyle.labelRes(): Int = when (this) {
    BatteryWidgetStyle.CIRCULAR -> R.string.style_circular
    BatteryWidgetStyle.HORIZONTAL -> R.string.style_horizontal
    BatteryWidgetStyle.PERCENTAGE -> R.string.style_large_percentage
    BatteryWidgetStyle.MINIMAL -> R.string.style_minimal_icon
}

@StringRes
fun StandByWidgetType.labelRes(): Int = when (this) {
    StandByWidgetType.CLOCK -> R.string.widget_clock
    StandByWidgetType.DATE -> R.string.widget_date
    StandByWidgetType.BATTERY -> R.string.widget_battery
    StandByWidgetType.CHARGING -> R.string.widget_charging
    StandByWidgetType.WORLD_CLOCK -> R.string.widget_world_clock
    StandByWidgetType.NEXT_ALARM -> R.string.widget_next_alarm
    StandByWidgetType.WEATHER -> R.string.widget_weather
    StandByWidgetType.MEDIA -> R.string.widget_media
}

@StringRes
fun ScreenTimeout.labelRes(): Int = when (this) {
    ScreenTimeout.SYSTEM_DEFAULT -> R.string.timeout_system
    ScreenTimeout.ONE_MINUTE -> R.string.timeout_one_minute
    ScreenTimeout.FIVE_MINUTES -> R.string.timeout_five_minutes
    ScreenTimeout.NEVER -> R.string.timeout_never
}

@StringRes
fun WorldClockZone.labelRes(): Int = when (this) {
    WorldClockZone.UTC -> R.string.city_utc
    WorldClockZone.ISTANBUL -> R.string.city_istanbul
    WorldClockZone.LONDON -> R.string.city_london
    WorldClockZone.PARIS -> R.string.city_paris
    WorldClockZone.NEW_YORK -> R.string.city_new_york
    WorldClockZone.LOS_ANGELES -> R.string.city_los_angeles
    WorldClockZone.DUBAI -> R.string.city_dubai
    WorldClockZone.NEW_DELHI -> R.string.city_new_delhi
    WorldClockZone.SINGAPORE -> R.string.city_singapore
    WorldClockZone.TOKYO -> R.string.city_tokyo
    WorldClockZone.SYDNEY -> R.string.city_sydney
}

fun Context.localizedLabel(value: Any): String = getString(
    when (value) {
        is ClockFace -> value.labelRes()
        is ClockColor -> value.labelRes()
        is StandByThemeId -> value.labelRes()
        is BackgroundStyle -> value.labelRes()
        is TypographyStyle -> value.labelRes()
        is ClockWidgetStyle -> value.labelRes()
        is DateWidgetStyle -> value.labelRes()
        is BatteryWidgetStyle -> value.labelRes()
        is StandByWidgetType -> value.labelRes()
        is ScreenTimeout -> value.labelRes()
        is WorldClockZone -> value.labelRes()
        else -> error("Unsupported label type: ${value::class.java.name}")
    }
)
