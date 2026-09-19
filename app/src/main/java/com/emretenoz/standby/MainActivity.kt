package com.emretenoz.standby

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.emretenoz.standby.data.settings.ScreenTimeout
import com.emretenoz.standby.data.settings.SettingsRepository
import com.emretenoz.standby.data.media.MediaRepository
import com.emretenoz.standby.data.weather.WeatherRepository
import com.emretenoz.standby.system.ChargingObserver
import com.emretenoz.standby.system.OrientationObserver
import com.emretenoz.standby.system.NextAlarmObserver
import com.emretenoz.standby.system.SystemStateRepository
import com.emretenoz.standby.system.StandByNotificationListener
import com.emretenoz.standby.ui.SettingsScreen
import com.emretenoz.standby.ui.StandByViewModel
import com.emretenoz.standby.ui.standby.StandByActions
import com.emretenoz.standby.ui.standby.StandByRoot
import com.emretenoz.standby.ui.theme.StandByTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var standByViewModel: StandByViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val applicationContext = applicationContext
        val settingsRepository = SettingsRepository(applicationContext)
        val systemStateRepository = SystemStateRepository(
            chargingObserver = ChargingObserver(applicationContext),
            orientationObserver = OrientationObserver(this),
            nextAlarmObserver = NextAlarmObserver(applicationContext),
            scope = lifecycleScope,
        )
        val mediaRepository = MediaRepository(applicationContext)
        val viewModel = ViewModelProvider(
            this,
            StandByViewModelFactory(
                settingsRepository,
                systemStateRepository,
                WeatherRepository(),
                mediaRepository,
            ),
        )[StandByViewModel::class.java]
        standByViewModel = viewModel

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            StandByTheme {
                StandByWindowBehavior(
                    active = state.isStandByActive,
                    keepAwake = state.settings.keepScreenAwake,
                    timeout = state.settings.screenTimeout,
                )
                StandByRoot(
                    state = state,
                    actions = StandByActions(
                        onExitPreview = if (state.isPreviewMode) viewModel::stopPreview else null,
                        onToggleWidget = viewModel::toggleWidget,
                        onMoveWidget = viewModel::moveWidget,
                        onClockFaceChanged = viewModel::setClockFace,
                        onClockColorChanged = viewModel::setClockColor,
                        onShowSecondsChanged = viewModel::setShowSeconds,
                        on24HourChanged = viewModel::setUse24HourClock,
                        onShowDateChanged = viewModel::setShowDate,
                        onDayThemeChanged = viewModel::setDayTheme,
                        onNightThemeChanged = viewModel::setNightTheme,
                        onBackgroundStyleChanged = viewModel::setBackgroundStyle,
                        onTypographyStyleChanged = viewModel::setTypographyStyle,
                        onCustomColorsChanged = viewModel::setCustomColors,
                        onClockWidgetStyleChanged = viewModel::setClockWidgetStyle,
                        onDateWidgetStyleChanged = viewModel::setDateWidgetStyle,
                        onBatteryWidgetStyleChanged = viewModel::setBatteryWidgetStyle,
                        onMediaPlayPause = viewModel::mediaPlayPause,
                        onMediaNext = viewModel::mediaNext,
                        onMediaPrevious = viewModel::mediaPrevious,
                    ),
                ) {
                    SettingsScreen(
                        state = state,
                        onAutomaticModeChanged = viewModel::setAutomaticModeEnabled,
                        on24HourChanged = viewModel::setUse24HourClock,
                        onShowSecondsChanged = viewModel::setShowSeconds,
                        onScreenTimeoutChanged = viewModel::setScreenTimeout,
                        onKeepAwakeChanged = viewModel::setKeepScreenAwake,
                        onClockFaceChanged = viewModel::setClockFace,
                        onClockColorChanged = viewModel::setClockColor,
                        onShowDateChanged = viewModel::setShowDate,
                        onNightModeChanged = viewModel::setNightMode,
                        onBurnInProtectionChanged = viewModel::setBurnInProtection,
                        onDayThemeChanged = viewModel::setDayTheme,
                        onNightThemeChanged = viewModel::setNightTheme,
                        onBackgroundStyleChanged = viewModel::setBackgroundStyle,
                        onTypographyStyleChanged = viewModel::setTypographyStyle,
                        onClockWidgetStyleChanged = viewModel::setClockWidgetStyle,
                        onDateWidgetStyleChanged = viewModel::setDateWidgetStyle,
                        onBatteryWidgetStyleChanged = viewModel::setBatteryWidgetStyle,
                        onWorldClockZoneChanged = viewModel::setWorldClockZone,
                        onWeatherEnabledChanged = viewModel::setWeatherEnabled,
                        onPreview = viewModel::startPreview,
                        onOpenScreenSaverSettings = ::openScreenSaverSettings,
                        onOpenMediaAccessSettings = ::openMediaAccessSettings,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::standByViewModel.isInitialized) standByViewModel.refreshMediaAccess()
    }

    private fun openScreenSaverSettings() {
        try {
            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                this,
                getString(R.string.screen_saver_settings_unavailable),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun openMediaAccessSettings() {
        val component = ComponentName(this, StandByNotificationListener::class.java)
        val detailIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                component.flattenToString(),
            )
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        try {
            startActivity(detailIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, R.string.media_settings_unavailable, Toast.LENGTH_LONG).show()
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun StandByWindowBehavior(
        active: Boolean,
        keepAwake: Boolean,
        timeout: ScreenTimeout,
    ) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        LaunchedEffect(active) {
            setShowWhenLocked(active)
            setTurnScreenOn(active)
            WindowCompat.setDecorFitsSystemWindows(window, !active)
            if (active) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        LaunchedEffect(active, keepAwake, timeout) {
            if (!active) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                return@LaunchedEffect
            }
            val holdDuration = when {
                keepAwake || timeout == ScreenTimeout.NEVER -> null
                timeout == ScreenTimeout.ONE_MINUTE -> 60_000L
                timeout == ScreenTimeout.FIVE_MINUTES -> 300_000L
                else -> 0L
            }
            if (holdDuration == 0L) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                if (holdDuration != null) {
                    delay(holdDuration)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                setShowWhenLocked(false)
                setTurnScreenOn(false)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(window, true)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

private class StandByViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val systemStateRepository: SystemStateRepository,
    private val weatherRepository: WeatherRepository,
    private val mediaRepository: MediaRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(StandByViewModel::class.java))
        return StandByViewModel(
            settingsRepository,
            systemStateRepository,
            weatherRepository,
            mediaRepository,
        ) as T
    }
}
