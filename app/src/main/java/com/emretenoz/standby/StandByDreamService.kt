package com.emretenoz.standby

import android.content.res.Configuration
import android.service.dreams.DreamService
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.emretenoz.standby.data.settings.StandByWidgetType
import com.emretenoz.standby.data.settings.WidgetColumn
import com.emretenoz.standby.data.settings.SettingsRepository
import com.emretenoz.standby.data.settings.StandBySettings
import com.emretenoz.standby.data.media.MediaRepository
import com.emretenoz.standby.data.media.MediaState
import com.emretenoz.standby.data.weather.WeatherRepository
import com.emretenoz.standby.data.weather.WeatherState
import com.emretenoz.standby.system.ChargingObserver
import com.emretenoz.standby.system.ChargingState
import com.emretenoz.standby.system.NextAlarmObserver
import com.emretenoz.standby.system.NextAlarmState
import com.emretenoz.standby.ui.standby.StandByActions
import com.emretenoz.standby.ui.standby.StandByDisplay
import com.emretenoz.standby.ui.theme.StandByTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class StandByDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var mediaRepository: MediaRepository

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    override fun onCreate() {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        super.onCreate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true
        isScreenBright = true

        val settingsRepository = SettingsRepository(applicationContext)
        val chargingObserver = ChargingObserver(applicationContext)
        val nextAlarmObserver = NextAlarmObserver(applicationContext)
        val weatherRepository = WeatherRepository()
        mediaRepository = MediaRepository(applicationContext)
        val content = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@StandByDreamService)
            setViewTreeViewModelStoreOwner(this@StandByDreamService)
            setViewTreeSavedStateRegistryOwner(this@StandByDreamService)
            setContent {
                val settings by settingsRepository.settings.collectAsState(StandBySettings())
                val charging by chargingObserver.state.collectAsState(ChargingState())
                val nextAlarm by nextAlarmObserver.state.collectAsState(NextAlarmState())
                val media by mediaRepository.state.collectAsState(MediaState())
                val weatherFlow = remember(settings.weatherEnabled, settings.worldClockZone) {
                    weatherRepository.observe(settings.weatherEnabled, settings.worldClockZone)
                }
                val weather by weatherFlow.collectAsState(WeatherState())
                val isLandscape = LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE

                StandByTheme {
                    if (isLandscape) {
                        StandByDisplay(
                            settings = settings,
                            charging = charging,
                            nextAlarm = nextAlarm,
                            weather = weather,
                            media = media,
                            actions = StandByActions(
                                onToggleWidget = { column: WidgetColumn, widget: StandByWidgetType ->
                                    serviceScope.launch { settingsRepository.toggleWidget(column, widget) }
                                },
                                onMoveWidget = { column, from, to ->
                                    serviceScope.launch { settingsRepository.moveWidget(column, from, to) }
                                },
                                onClockFaceChanged = { face ->
                                    serviceScope.launch { settingsRepository.setClockFace(face) }
                                },
                                onClockColorChanged = { color ->
                                    serviceScope.launch { settingsRepository.setClockColor(color) }
                                },
                                onShowSecondsChanged = { value ->
                                    serviceScope.launch { settingsRepository.setShowSeconds(value) }
                                },
                                on24HourChanged = { value ->
                                    serviceScope.launch { settingsRepository.setUse24HourClock(value) }
                                },
                                onShowDateChanged = { value ->
                                    serviceScope.launch { settingsRepository.setShowDate(value) }
                                },
                                onDayThemeChanged = { value ->
                                    serviceScope.launch { settingsRepository.setDayTheme(value) }
                                },
                                onNightThemeChanged = { value ->
                                    serviceScope.launch { settingsRepository.setNightTheme(value) }
                                },
                                onBackgroundStyleChanged = { value ->
                                    serviceScope.launch { settingsRepository.setBackgroundStyle(value) }
                                },
                                onTypographyStyleChanged = { value ->
                                    serviceScope.launch { settingsRepository.setTypographyStyle(value) }
                                },
                                onCustomColorsChanged = { background, primary, secondary, accent ->
                                    serviceScope.launch {
                                        settingsRepository.setCustomColors(background, primary, secondary, accent)
                                    }
                                },
                                onClockWidgetStyleChanged = { value ->
                                    serviceScope.launch { settingsRepository.setClockWidgetStyle(value) }
                                },
                                onDateWidgetStyleChanged = { value ->
                                    serviceScope.launch { settingsRepository.setDateWidgetStyle(value) }
                                },
                                onBatteryWidgetStyleChanged = { value ->
                                    serviceScope.launch { settingsRepository.setBatteryWidgetStyle(value) }
                                },
                                onMediaPlayPause = mediaRepository::playPause,
                                onMediaNext = mediaRepository::skipToNext,
                                onMediaPrevious = mediaRepository::skipToPrevious,
                            ),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.rotate_to_landscape),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        setContentView(content)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDreamingStopped() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onDreamingStopped()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        if (::mediaRepository.isInitialized) mediaRepository.close()
        serviceScope.cancel()
        super.onDestroy()
    }
}
