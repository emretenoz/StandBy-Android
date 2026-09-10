package com.example.standby.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.standby.data.settings.ScreenTimeout
import com.example.standby.data.settings.ClockColor
import com.example.standby.data.settings.ClockFace
import com.example.standby.data.settings.BackgroundStyle
import com.example.standby.data.settings.BatteryWidgetStyle
import com.example.standby.data.settings.ClockWidgetStyle
import com.example.standby.data.settings.DateWidgetStyle
import com.example.standby.data.settings.StandByThemeId
import com.example.standby.data.settings.TypographyStyle
import com.example.standby.data.settings.SettingsRepository
import com.example.standby.data.settings.StandBySettings
import com.example.standby.data.settings.StandByWidgetType
import com.example.standby.data.settings.WidgetColumn
import com.example.standby.system.ChargingState
import com.example.standby.system.SystemStateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StandByUiState(
    val settings: StandBySettings = StandBySettings(),
    val charging: ChargingState = ChargingState(),
    val isLandscape: Boolean = false,
    val isStandByActive: Boolean = false,
    val isPreviewMode: Boolean = false,
)

class StandByViewModel(
    private val settingsRepository: SettingsRepository,
    systemStateRepository: SystemStateRepository,
) : ViewModel() {
    private val previewMode = MutableStateFlow(false)

    val uiState: StateFlow<StandByUiState> = combine(
        settingsRepository.settings,
        systemStateRepository.charging,
        systemStateRepository.isLandscape,
        previewMode,
    ) { settings, charging, isLandscape, isPreviewMode ->
        StandByUiState(
            settings = settings,
            charging = charging,
            isLandscape = isLandscape,
            isStandByActive = isPreviewMode ||
                (settings.automaticModeEnabled && charging.isCharging && isLandscape),
            isPreviewMode = isPreviewMode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StandByUiState())

    fun setAutomaticModeEnabled(value: Boolean) = update { setAutomaticModeEnabled(value) }
    fun setUse24HourClock(value: Boolean) = update { setUse24HourClock(value) }
    fun setShowSeconds(value: Boolean) = update { setShowSeconds(value) }
    fun setKeepScreenAwake(value: Boolean) = update { setKeepScreenAwake(value) }
    fun setClockFace(value: ClockFace) = update { setClockFace(value) }
    fun setClockColor(value: ClockColor) = update { setClockColor(value) }
    fun setShowDate(value: Boolean) = update { setShowDate(value) }
    fun setNightMode(value: Boolean) = update { setNightMode(value) }
    fun setBurnInProtection(value: Boolean) = update { setBurnInProtection(value) }
    fun setDayTheme(value: StandByThemeId) = update { setDayTheme(value) }
    fun setNightTheme(value: StandByThemeId) = update { setNightTheme(value) }
    fun setBackgroundStyle(value: BackgroundStyle) = update { setBackgroundStyle(value) }
    fun setTypographyStyle(value: TypographyStyle) = update { setTypographyStyle(value) }
    fun setClockWidgetStyle(value: ClockWidgetStyle) = update { setClockWidgetStyle(value) }
    fun setDateWidgetStyle(value: DateWidgetStyle) = update { setDateWidgetStyle(value) }
    fun setBatteryWidgetStyle(value: BatteryWidgetStyle) = update { setBatteryWidgetStyle(value) }
    fun setCustomColors(background: Long, primary: Long, secondary: Long, accent: Long) =
        update { setCustomColors(background, primary, secondary, accent) }
    fun setScreenTimeout(value: ScreenTimeout) = update { setScreenTimeout(value) }
    fun startPreview() { previewMode.value = true }
    fun stopPreview() { previewMode.value = false }
    fun toggleWidget(column: WidgetColumn, widget: StandByWidgetType) =
        update { toggleWidget(column, widget) }
    fun moveWidget(column: WidgetColumn, fromIndex: Int, toIndex: Int) =
        update { moveWidget(column, fromIndex, toIndex) }

    private fun update(block: suspend SettingsRepository.() -> Unit) {
        viewModelScope.launch { settingsRepository.block() }
    }
}
