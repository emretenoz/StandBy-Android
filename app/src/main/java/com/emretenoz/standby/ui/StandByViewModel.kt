package com.emretenoz.standby.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emretenoz.standby.data.settings.ScreenTimeout
import com.emretenoz.standby.data.settings.ClockColor
import com.emretenoz.standby.data.settings.ClockFace
import com.emretenoz.standby.data.settings.BackgroundStyle
import com.emretenoz.standby.data.settings.BatteryWidgetStyle
import com.emretenoz.standby.data.settings.ClockWidgetStyle
import com.emretenoz.standby.data.settings.DateWidgetStyle
import com.emretenoz.standby.data.settings.StandByThemeId
import com.emretenoz.standby.data.settings.TypographyStyle
import com.emretenoz.standby.data.settings.SettingsRepository
import com.emretenoz.standby.data.settings.StandBySettings
import com.emretenoz.standby.data.settings.StandByWidgetType
import com.emretenoz.standby.data.settings.WidgetColumn
import com.emretenoz.standby.data.settings.WorldClockZone
import com.emretenoz.standby.data.media.MediaRepository
import com.emretenoz.standby.data.media.MediaState
import com.emretenoz.standby.data.weather.WeatherRepository
import com.emretenoz.standby.data.weather.WeatherState
import com.emretenoz.standby.system.ChargingState
import com.emretenoz.standby.system.SystemStateRepository
import com.emretenoz.standby.system.NextAlarmState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StandByUiState(
    val settings: StandBySettings = StandBySettings(),
    val charging: ChargingState = ChargingState(),
    val isLandscape: Boolean = false,
    val nextAlarm: NextAlarmState = NextAlarmState(),
    val weather: WeatherState = WeatherState(),
    val media: MediaState = MediaState(),
    val isStandByActive: Boolean = false,
    val isPreviewMode: Boolean = false,
)

internal fun shouldActivateStandBy(
    automaticModeEnabled: Boolean,
    isCharging: Boolean,
    isLandscape: Boolean,
    isPreviewMode: Boolean,
): Boolean = isPreviewMode || (automaticModeEnabled && isCharging && isLandscape)

class StandByViewModel(
    private val settingsRepository: SettingsRepository,
    systemStateRepository: SystemStateRepository,
    weatherRepository: WeatherRepository,
    private val mediaRepository: MediaRepository,
) : ViewModel() {
    private val previewMode = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val weatherState = settingsRepository.settings
        .map { it.weatherEnabled to it.worldClockZone }
        .distinctUntilChanged()
        .flatMapLatest { (enabled, location) -> weatherRepository.observe(enabled, location) }

    private val runtimeState = combine(
        systemStateRepository.charging,
        systemStateRepository.isLandscape,
        systemStateRepository.nextAlarm,
        mediaRepository.state,
    ) { charging, isLandscape, nextAlarm, media ->
        RuntimeState(charging, isLandscape, nextAlarm, media)
    }

    private val baseState = combine(
        settingsRepository.settings,
        weatherState,
        runtimeState,
    ) { settings, weather, runtime ->
        StandByUiState(
            settings = settings,
            charging = runtime.charging,
            isLandscape = runtime.isLandscape,
            nextAlarm = runtime.nextAlarm,
            weather = weather,
            media = runtime.media,
        )
    }

    val uiState: StateFlow<StandByUiState> = combine(baseState, previewMode) { state, isPreviewMode ->
        state.copy(
            isStandByActive = shouldActivateStandBy(
                automaticModeEnabled = state.settings.automaticModeEnabled,
                isCharging = state.charging.isCharging,
                isLandscape = state.isLandscape,
                isPreviewMode = isPreviewMode,
            ),
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
    fun setWorldClockZone(value: WorldClockZone) = update { setWorldClockZone(value) }
    fun setWeatherEnabled(value: Boolean) = update { setWeatherEnabled(value) }
    fun refreshMediaAccess() = mediaRepository.refresh()
    fun mediaPlayPause() = mediaRepository.playPause()
    fun mediaNext() = mediaRepository.skipToNext()
    fun mediaPrevious() = mediaRepository.skipToPrevious()
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

    override fun onCleared() {
        mediaRepository.close()
        super.onCleared()
    }
}

private data class RuntimeState(
    val charging: ChargingState,
    val isLandscape: Boolean,
    val nextAlarm: NextAlarmState,
    val media: MediaState,
)
