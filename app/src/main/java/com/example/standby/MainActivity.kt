package com.example.standby

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
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
import com.example.standby.data.settings.ScreenTimeout
import com.example.standby.data.settings.SettingsRepository
import com.example.standby.system.ChargingObserver
import com.example.standby.system.OrientationObserver
import com.example.standby.system.SystemStateRepository
import com.example.standby.ui.SettingsScreen
import com.example.standby.ui.StandByRoot
import com.example.standby.ui.StandByViewModel
import com.example.standby.ui.theme.StandByTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val applicationContext = applicationContext
        val settingsRepository = SettingsRepository(applicationContext)
        val systemStateRepository = SystemStateRepository(
            chargingObserver = ChargingObserver(applicationContext),
            orientationObserver = OrientationObserver(this),
            scope = lifecycleScope,
        )
        val viewModel = ViewModelProvider(
            this,
            StandByViewModelFactory(settingsRepository, systemStateRepository),
        )[StandByViewModel::class.java]

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
                    onExitPreview = viewModel::stopPreview,
                    onToggleWidget = viewModel::toggleWidget,
                ) {
                    SettingsScreen(
                        state = state,
                        onAutomaticModeChanged = viewModel::setAutomaticModeEnabled,
                        on24HourChanged = viewModel::setUse24HourClock,
                        onShowSecondsChanged = viewModel::setShowSeconds,
                        onScreenTimeoutChanged = viewModel::setScreenTimeout,
                        onKeepAwakeChanged = viewModel::setKeepScreenAwake,
                        onPreview = viewModel::startPreview,
                        onOpenScreenSaverSettings = {
                            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                        },
                    )
                }
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
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(StandByViewModel::class.java))
        return StandByViewModel(settingsRepository, systemStateRepository) as T
    }
}
