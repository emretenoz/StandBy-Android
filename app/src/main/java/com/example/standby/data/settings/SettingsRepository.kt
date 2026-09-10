package com.example.standby.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
}
