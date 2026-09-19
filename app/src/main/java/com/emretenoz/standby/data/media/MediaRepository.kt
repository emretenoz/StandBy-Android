package com.emretenoz.standby.data.media

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import com.emretenoz.standby.system.StandByNotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaState(
    val accessGranted: Boolean = false,
    val hasSession: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val isPlaying: Boolean = false,
)

class MediaRepository(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
    private val sessionManager = appContext.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(appContext, StandByNotificationListener::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val mutableState = MutableStateFlow(MediaState())
    val state: StateFlow<MediaState> = mutableState.asStateFlow()

    private var registered = false
    private var activeController: MediaController? = null
    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish(activeController)
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish(activeController)
        override fun onSessionDestroyed() = refresh()
    }
    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        selectController(controllers.orEmpty())
    }

    init {
        refresh()
    }

    fun refresh() {
        val granted = notificationManager.isNotificationListenerAccessGranted(listenerComponent)
        if (!granted) {
            unregisterSessionListener()
            selectController(emptyList())
            mutableState.value = MediaState(accessGranted = false)
            return
        }

        if (!registered) {
            runCatching {
                sessionManager.addOnActiveSessionsChangedListener(
                    sessionsListener,
                    listenerComponent,
                    handler,
                )
                registered = true
            }.onFailure {
                mutableState.value = MediaState(accessGranted = true)
                return
            }
        }
        val controllers = runCatching { sessionManager.getActiveSessions(listenerComponent) }
            .getOrElse { emptyList() }
        selectController(controllers)
    }

    fun playPause() {
        activeController?.let { controller ->
            if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        }
    }

    fun skipToNext() = activeController?.transportControls?.skipToNext() ?: Unit
    fun skipToPrevious() = activeController?.transportControls?.skipToPrevious() ?: Unit

    private fun selectController(controllers: List<MediaController>) {
        val selected = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull()
        if (selected?.sessionToken != activeController?.sessionToken) {
            activeController?.unregisterCallback(controllerCallback)
            activeController = selected
            selected?.registerCallback(controllerCallback, handler)
        }
        publish(selected)
    }

    private fun publish(controller: MediaController?) {
        val metadata = controller?.metadata
        mutableState.value = MediaState(
            accessGranted = notificationManager.isNotificationListenerAccessGranted(listenerComponent),
            hasSession = controller != null,
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata?.description?.title?.toString(),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata?.description?.subtitle?.toString(),
            isPlaying = controller?.playbackState?.state == PlaybackState.STATE_PLAYING,
        )
    }

    private fun unregisterSessionListener() {
        if (registered) {
            sessionManager.removeOnActiveSessionsChangedListener(sessionsListener)
            registered = false
        }
    }

    override fun close() {
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
        unregisterSessionListener()
    }
}
