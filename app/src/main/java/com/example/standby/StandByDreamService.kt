package com.example.standby

import android.content.res.Configuration
import android.service.dreams.DreamService
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.standby.data.settings.StandByWidgetType
import com.example.standby.data.settings.WidgetColumn
import com.example.standby.data.settings.SettingsRepository
import com.example.standby.data.settings.StandBySettings
import com.example.standby.system.ChargingObserver
import com.example.standby.system.ChargingState
import com.example.standby.ui.StandByDisplay
import com.example.standby.ui.theme.StandByTheme
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
        val content = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@StandByDreamService)
            setViewTreeViewModelStoreOwner(this@StandByDreamService)
            setViewTreeSavedStateRegistryOwner(this@StandByDreamService)
            setContent {
                val settings by settingsRepository.settings.collectAsState(StandBySettings())
                val charging by chargingObserver.state.collectAsState(ChargingState())
                val isLandscape = LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE

                StandByTheme {
                    if (isLandscape) {
                        StandByDisplay(
                            settings = settings,
                            charging = charging,
                            onExitPreview = null,
                            onToggleWidget = { column: WidgetColumn, widget: StandByWidgetType ->
                                serviceScope.launch {
                                    settingsRepository.toggleWidget(column, widget)
                                }
                            },
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "ROTATE TO LANDSCAPE",
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
        serviceScope.cancel()
        super.onDestroy()
    }
}
