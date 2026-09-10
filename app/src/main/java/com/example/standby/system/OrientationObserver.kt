package com.example.standby.system

import android.content.res.Configuration
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class OrientationObserver(private val activity: ComponentActivity) {
    val isLandscape: Flow<Boolean> = callbackFlow {
        var sensorHasReading = false

        fun emitCurrent(configuration: Configuration = activity.resources.configuration) {
            if (!sensorHasReading) {
                trySend(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            }
        }

        val listener = androidx.core.util.Consumer<Configuration> { emitCurrent(it) }
        val orientationListener = object : OrientationEventListener(activity) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    sensorHasReading = false
                    emitCurrent()
                    return
                }
                sensorHasReading = true
                val physicallyLandscape = orientation in 45..134 || orientation in 225..314
                trySend(physicallyLandscape)
            }
        }
        activity.addOnConfigurationChangedListener(listener)
        if (orientationListener.canDetectOrientation()) orientationListener.enable()
        emitCurrent()
        awaitClose {
            orientationListener.disable()
            activity.removeOnConfigurationChangedListener(listener)
        }
    }.distinctUntilChanged()
}
