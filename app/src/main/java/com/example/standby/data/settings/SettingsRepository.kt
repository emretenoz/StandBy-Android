package com.example.standby.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "standby_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val automaticMode = booleanPreferencesKey("automatic_mode")
        val use24Hour = booleanPreferencesKey("use_24_hour")
        val showSeconds = booleanPreferencesKey("show_seconds")
        val screenTimeout = stringPreferencesKey("screen_timeout")
        val keepScreenAwake = booleanPreferencesKey("keep_screen_awake")
        val leftWidgets = stringPreferencesKey("left_widgets")
        val rightWidgets = stringPreferencesKey("right_widgets")
        val clockFace = stringPreferencesKey("clock_face")
        val clockColor = stringPreferencesKey("clock_color")
        val showDate = booleanPreferencesKey("show_date")
        val nightMode = booleanPreferencesKey("night_mode")
        val burnInProtection = booleanPreferencesKey("burn_in_protection")
        val dayTheme = stringPreferencesKey("day_theme")
        val nightTheme = stringPreferencesKey("night_theme")
        val backgroundStyle = stringPreferencesKey("background_style")
        val typographyStyle = stringPreferencesKey("typography_style")
        val customBackground = longPreferencesKey("custom_background")
        val customPrimary = longPreferencesKey("custom_primary")
        val customSecondary = longPreferencesKey("custom_secondary")
        val customAccent = longPreferencesKey("custom_accent")
        val clockWidgetStyle = stringPreferencesKey("clock_widget_style")
        val dateWidgetStyle = stringPreferencesKey("date_widget_style")
        val batteryWidgetStyle = stringPreferencesKey("battery_widget_style")
    }

    val settings: Flow<StandBySettings> = context.settingsDataStore.data.map { preferences ->
        StandBySettings(
            automaticModeEnabled = preferences[Keys.automaticMode] ?: true,
            use24HourClock = preferences[Keys.use24Hour] ?: true,
            showSeconds = preferences[Keys.showSeconds] ?: false,
            screenTimeout = preferences[Keys.screenTimeout]
                ?.let { saved -> ScreenTimeout.entries.firstOrNull { it.name == saved } }
                ?: ScreenTimeout.SYSTEM_DEFAULT,
            keepScreenAwake = preferences[Keys.keepScreenAwake] ?: true,
            clockFace = preferences[Keys.clockFace]
                ?.let { saved -> ClockFace.entries.firstOrNull { it.name == saved } }
                ?: ClockFace.DIGITAL,
            clockColor = preferences[Keys.clockColor]
                ?.let { saved -> ClockColor.entries.firstOrNull { it.name == saved } }
                ?: ClockColor.INHERIT,
            showDate = preferences[Keys.showDate] ?: true,
            nightMode = preferences[Keys.nightMode] ?: false,
            burnInProtection = preferences[Keys.burnInProtection] ?: true,
            dayTheme = enumValue(preferences[Keys.dayTheme], StandByThemeId.CLASSIC),
            nightTheme = enumValue(preferences[Keys.nightTheme], StandByThemeId.CRIMSON_NIGHT),
            backgroundStyle = enumValue(preferences[Keys.backgroundStyle], BackgroundStyle.SOLID),
            typographyStyle = enumValue(preferences[Keys.typographyStyle], TypographyStyle.AUTO),
            customBackground = preferences[Keys.customBackground] ?: 0xFF000000,
            customPrimary = preferences[Keys.customPrimary] ?: 0xFFF1EDE3,
            customSecondary = preferences[Keys.customSecondary] ?: 0xFF9D9990,
            customAccent = preferences[Keys.customAccent] ?: 0xFFFF9F0A,
            clockWidgetStyle = enumValue(preferences[Keys.clockWidgetStyle], ClockWidgetStyle.MINIMAL),
            dateWidgetStyle = enumValue(preferences[Keys.dateWidgetStyle], DateWidgetStyle.EDITORIAL),
            batteryWidgetStyle = enumValue(preferences[Keys.batteryWidgetStyle], BatteryWidgetStyle.CIRCULAR),
            leftWidgets = decodeWidgets(
                preferences[Keys.leftWidgets],
                listOf(StandByWidgetType.CLOCK, StandByWidgetType.BATTERY),
            ),
            rightWidgets = decodeWidgets(
                preferences[Keys.rightWidgets],
                listOf(StandByWidgetType.DATE, StandByWidgetType.CHARGING),
            ),
        )
    }

    suspend fun setAutomaticModeEnabled(enabled: Boolean) = update(Keys.automaticMode, enabled)
    suspend fun setUse24HourClock(enabled: Boolean) = update(Keys.use24Hour, enabled)
    suspend fun setShowSeconds(enabled: Boolean) = update(Keys.showSeconds, enabled)
    suspend fun setKeepScreenAwake(enabled: Boolean) = update(Keys.keepScreenAwake, enabled)
    suspend fun setShowDate(enabled: Boolean) = update(Keys.showDate, enabled)
    suspend fun setNightMode(enabled: Boolean) = update(Keys.nightMode, enabled)
    suspend fun setBurnInProtection(enabled: Boolean) = update(Keys.burnInProtection, enabled)

    suspend fun setClockFace(face: ClockFace) {
        context.settingsDataStore.edit { it[Keys.clockFace] = face.name }
    }

    suspend fun setClockColor(color: ClockColor) {
        context.settingsDataStore.edit { it[Keys.clockColor] = color.name }
    }

    suspend fun setDayTheme(theme: StandByThemeId) = setEnum(Keys.dayTheme, theme)
    suspend fun setNightTheme(theme: StandByThemeId) = setEnum(Keys.nightTheme, theme)
    suspend fun setBackgroundStyle(style: BackgroundStyle) = setEnum(Keys.backgroundStyle, style)
    suspend fun setTypographyStyle(style: TypographyStyle) = setEnum(Keys.typographyStyle, style)
    suspend fun setClockWidgetStyle(style: ClockWidgetStyle) = setEnum(Keys.clockWidgetStyle, style)
    suspend fun setDateWidgetStyle(style: DateWidgetStyle) = setEnum(Keys.dateWidgetStyle, style)
    suspend fun setBatteryWidgetStyle(style: BatteryWidgetStyle) = setEnum(Keys.batteryWidgetStyle, style)

    suspend fun setCustomColors(background: Long, primary: Long, secondary: Long, accent: Long) {
        context.settingsDataStore.edit {
            it[Keys.customBackground] = background
            it[Keys.customPrimary] = primary
            it[Keys.customSecondary] = secondary
            it[Keys.customAccent] = accent
        }
    }

    suspend fun setScreenTimeout(timeout: ScreenTimeout) {
        context.settingsDataStore.edit { it[Keys.screenTimeout] = timeout.name }
    }

    suspend fun toggleWidget(column: WidgetColumn, widget: StandByWidgetType) {
        val key = if (column == WidgetColumn.LEFT) Keys.leftWidgets else Keys.rightWidgets
        val defaults = if (column == WidgetColumn.LEFT) {
            listOf(StandByWidgetType.CLOCK, StandByWidgetType.BATTERY)
        } else {
            listOf(StandByWidgetType.DATE, StandByWidgetType.CHARGING)
        }
        context.settingsDataStore.edit { preferences ->
            val widgets = decodeWidgets(preferences[key], defaults).toMutableList()
            if (widget in widgets) {
                if (widgets.size > 1) widgets.remove(widget)
            } else {
                widgets.add(widget)
            }
            preferences[key] = widgets.joinToString(",") { it.name }
        }
    }

    suspend fun moveWidget(column: WidgetColumn, fromIndex: Int, toIndex: Int) {
        val key = if (column == WidgetColumn.LEFT) Keys.leftWidgets else Keys.rightWidgets
        val defaults = if (column == WidgetColumn.LEFT) {
            listOf(StandByWidgetType.CLOCK, StandByWidgetType.BATTERY)
        } else {
            listOf(StandByWidgetType.DATE, StandByWidgetType.CHARGING)
        }
        context.settingsDataStore.edit { preferences ->
            val widgets = decodeWidgets(preferences[key], defaults).toMutableList()
            if (fromIndex !in widgets.indices || toIndex !in widgets.indices) return@edit
            val moved = widgets.removeAt(fromIndex)
            widgets.add(toIndex, moved)
            preferences[key] = widgets.joinToString(",") { it.name }
        }
    }

    private suspend fun update(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        context.settingsDataStore.edit { it[key] = value }
    }


    private fun decodeWidgets(
        encoded: String?,
        defaults: List<StandByWidgetType>,
    ): List<StandByWidgetType> {
        val decoded = encoded
            ?.split(',')
            ?.mapNotNull { name -> StandByWidgetType.entries.firstOrNull { it.name == name } }
            ?.distinct()
            .orEmpty()
        return decoded.ifEmpty { defaults }
    }

    private inline fun <reified T : Enum<T>> enumValue(saved: String?, fallback: T): T =
        saved?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: fallback

    private suspend fun setEnum(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: Enum<*>) {
        context.settingsDataStore.edit { it[key] = value.name }
    }
}
