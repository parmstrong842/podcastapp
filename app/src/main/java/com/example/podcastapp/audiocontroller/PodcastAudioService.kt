package com.example.podcastapp.audiocontroller


import android.content.Intent
import android.content.Intent.EXTRA_KEY_EVENT
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_MEDIA_NEXT
import android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MIN_BUFFER_MS
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.R
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min


private const val tag = "PodcastAudioService"

class PodcastAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    private lateinit var databaseRepository: DatabaseRepository
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressUpdateJob: Job? = null
    private var currentMediaItem: MediaItem? = null
    private var maxBufferMs = 1000 * 60 * 10

    private val SPEED_0_5X = "SPEED_0_5X"
    private val SPEED_0_7X = "SPEED_0_7X"
    private val SPEED_1X = "SPEED_1X"
    private val SPEED_1_2X = "SPEED_1_2X"
    private val SPEED_1_5X = "SPEED_1_5X"
    private val SPEED_1_7X = "SPEED_1_7X"
    private val SPEED_2X = "SPEED_2X"
    private val SEEK_BACK = "SEEK_BACK"
    private val SEEK_FORWARD = "SEEK_FORWARD"
    private val SEEK_TO_PREVIOUS = "SEEK_TO_PREVIOUS"

    private val speed_0_5xButton = CommandButton.Builder()
        .setDisplayName("Speed up")
        .setIconResId(R.drawable.speed_0_5x_24)
        .setSessionCommand(SessionCommand(SPEED_0_5X, Bundle()))
        .build()
    private val speed_0_7xButton = CommandButton.Builder()
        .setDisplayName("Speed up")
        .setIconResId(R.drawable.speed_0_7x_24)
        .setSessionCommand(SessionCommand(SPEED_0_7X, Bundle()))
        .build()
    private val speed_1xButton = CommandButton.Builder()
        .setDisplayName("Speed up")
        .setIconResId(R.drawable.speed_1x_24)
        .setSessionCommand(SessionCommand(SPEED_1X, Bundle()))
        .build()
    private val speed_1_2xButton = CommandButton.Builder()
        .setDisplayName("Speed up")
        .setIconResId(R.drawable.speed_1_2x_24)
        .setSessionCommand(SessionCommand(SPEED_1_2X, Bundle()))
        .build()
    private val speed_1_5xButton = CommandButton.Builder()
        .setDisplayName("Speed up")
        .setIconResId(R.drawable.speed_1_5x_24)
        .setSessionCommand(SessionCommand(SPEED_1_5X, Bundle()))
        .build()
    private val speed_1_7xButton = CommandButton.Builder()
        .setDisplayName("Speed up")
        .setIconResId(R.drawable.speed_1_7x_24)
        .setSessionCommand(SessionCommand(SPEED_1_7X, Bundle()))
        .build()
    private val speed_2xButton = CommandButton.Builder()
        .setDisplayName("Speed down")
        .setIconResId(R.drawable.speed_2x_24)
        .setSessionCommand(SessionCommand(SPEED_2X, Bundle()))
        .build()
    val seekBackButton = CommandButton.Builder()
        .setDisplayName("Seek back")
        .setIconResId(R.drawable.baseline_replay_10_24)
        .setSessionCommand(SessionCommand(SEEK_BACK, Bundle()))
        .build()
    val seekForwardButton = CommandButton.Builder()
        .setDisplayName("Seek forward")
        .setIconResId(R.drawable.baseline_forward_30_24)
        .setSessionCommand(SessionCommand(SEEK_FORWARD, Bundle()))
        .build()
    val seekToPreviousButton = CommandButton.Builder()
        .setDisplayName("Seek to previous")
        .setIconResId(R.drawable.baseline_skip_previous_24)
        .setSessionCommand(SessionCommand(SEEK_TO_PREVIOUS, Bundle()))
        .build()

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        databaseRepository = (application as PodcastApplication).container.databaseRepository

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DEFAULT_MIN_BUFFER_MS,
                maxBufferMs,
                DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            )
            .build()

        val player = ExoPlayer.Builder(this).apply {
            setLoadControl(loadControl)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            setHandleAudioBecomingNoisy(true)
        }.build()
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    currentMediaItem = it
                }
                if (mediaItem == null) {
                    Log.d(tag, "onMediaItemTransition received null, ignoring update.")
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(tag, "onIsPlayingChanged: $isPlaying")
                if (isPlaying) {
                    // backup in case other methods fail to save progress
                    if (progressUpdateJob == null || progressUpdateJob?.isCancelled == true) {
                        progressUpdateJob = coroutineScope.launch {
                            while (isActive) {
                                delay(60000)
                                currentMediaItem?.let {
                                    coroutineScope.launch {
                                        Log.d(tag, "1")
                                        saveCurrentProgress(
                                            currentMediaItem = it,
                                            currentPosition = mediaSession?.player?.currentPosition ?: 0,
                                            totalDuration = mediaSession?.player?.duration ?: 0
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    progressUpdateJob?.cancel()
                    progressUpdateJob = null
                    currentMediaItem?.let {
                        coroutineScope.launch {
                            Log.d(tag, "2")
                            saveCurrentProgress(
                                currentMediaItem = it,
                                currentPosition = mediaSession?.player?.currentPosition ?: 0,
                                totalDuration = mediaSession?.player?.duration ?: 0
                            )
                        }
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                //Log.d(tag, reason.toString())
                if (reason == Player.DISCONTINUITY_REASON_REMOVE || reason == Player.DISCONTINUITY_REASON_SEEK) {
                    currentMediaItem?.let {
                        coroutineScope.launch {
                            Log.d(tag, "3")
                            saveCurrentProgress(
                                currentMediaItem = it,
                                currentPosition = oldPosition.positionMs,
                                totalDuration = mediaSession?.player?.duration ?: 0 // TODO: might not be correct
                            )
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    currentMediaItem?.let {
                        coroutineScope.launch {
                            Log.d(tag, "currentPosition: ${mediaSession?.player?.currentPosition}, duration: ${mediaSession?.player?.duration}")
                            Log.d(tag, "4")
                            saveCurrentProgress(
                                currentMediaItem = it,
                                currentPosition = mediaSession?.player?.currentPosition ?: 0, // TODO:
                                totalDuration = mediaSession?.player?.duration ?: 0,
                                finished = true
                            )
                        }
                    }
                }
            }
        })
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(CustomMediaSessionCallback())
            .setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1xButton, seekToPreviousButton))
            .build()
    }

    private suspend fun saveCurrentProgress(
        currentMediaItem: MediaItem,
        currentPosition: Long,
        totalDuration: Long,
        finished: Boolean = false
    ) {
        Log.d(tag, "save")
        val extras = currentMediaItem.mediaMetadata.extras ?: return

        val feedUrl = extras.getString("FEED_URL") ?: return
        val guid = extras.getLong("GUID")

        try {
            // TODO: position or duration is messed up
            withContext(Dispatchers.IO) {
                databaseRepository.saveProgress(
                    PodcastProgressEntity(
                        feedUrl = feedUrl,
                        guid = guid,
                        position = currentPosition,
                        duration = totalDuration,
                        finished = finished
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to save progress", e)
        }
    }

    @UnstableApi
    private inner class CustomMediaSessionCallback: MediaSession.Callback {

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            val keyEvent: KeyEvent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.extras?.getParcelable(EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.extras?.getParcelable(EXTRA_KEY_EVENT) as? KeyEvent
            }

            if (keyEvent == null) return false

            return when (keyEvent.keyCode) {
                KEYCODE_MEDIA_PREVIOUS -> {
                    seekBack(session)
                    true
                }
                KEYCODE_MEDIA_NEXT -> {
                    seekForward(session)
                    true
                }
                else -> false
            }
        }

        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ConnectionResult {
            val sessionCommands = ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(SPEED_0_5X, Bundle.EMPTY))
                .add(SessionCommand(SPEED_0_7X, Bundle.EMPTY))
                .add(SessionCommand(SPEED_1X, Bundle.EMPTY))
                .add(SessionCommand(SPEED_1_2X, Bundle.EMPTY))
                .add(SessionCommand(SPEED_1_5X, Bundle.EMPTY))
                .add(SessionCommand(SPEED_1_7X, Bundle.EMPTY))
                .add(SessionCommand(SPEED_2X, Bundle.EMPTY))
                .add(SessionCommand(SEEK_BACK, Bundle.EMPTY))
                .add(SessionCommand(SEEK_FORWARD, Bundle.EMPTY))
                .add(SessionCommand(SEEK_TO_PREVIOUS, Bundle.EMPTY))
                .build()
            return ConnectionResult.AcceptedResultBuilder(session)
                .setAvailablePlayerCommands(
                    ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                        .remove(COMMAND_SEEK_TO_PREVIOUS)
                        .build()
                )
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.d(tag, "Received custom command: ${customCommand.customAction}")
            when (customCommand.customAction) {
                SPEED_0_5X -> {
                    session.player.playbackParameters = PlaybackParameters(0.7f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_0_7xButton, seekToPreviousButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_0_7X -> {
                    session.player.playbackParameters = PlaybackParameters(1f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1xButton, seekToPreviousButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1X -> {
                    session.player.playbackParameters = PlaybackParameters(1.2f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1_2xButton, seekToPreviousButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1_2X -> {
                    session.player.playbackParameters = PlaybackParameters(1.5f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1_5xButton, seekToPreviousButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1_5X -> {
                    session.player.playbackParameters = PlaybackParameters(1.7f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1_7xButton, seekToPreviousButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1_7X -> {
                    session.player.playbackParameters = PlaybackParameters(2f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_2xButton, seekToPreviousButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_2X -> {
                    session.player.playbackParameters = PlaybackParameters(0.5f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_0_5xButton, seekToPreviousButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SEEK_BACK -> {
                    seekBack(session)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SEEK_FORWARD -> {
                    seekForward(session)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SEEK_TO_PREVIOUS -> {
                    seekToPrevious(session)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        private fun seekBack(session: MediaSession) {
            val currentPosition = session.player.currentPosition
            var seekPosition = currentPosition - 10000
            if (seekPosition < 0) {
                seekPosition = 0
            }
            session.player.seekTo(seekPosition)
        }

        private fun seekForward(session: MediaSession) {
            val currentPosition = session.player.currentPosition
            val duration = session.player.duration
            var seekPosition = currentPosition + 30000
            if (duration != C.TIME_UNSET) {
                seekPosition = min(seekPosition.toDouble(), duration.toDouble()).toLong()
            }
            session.player.seekTo(seekPosition)
        }

        private fun seekToPrevious(session: MediaSession) {
            val player = session.player
            if (player.currentPosition > 3000) {
                player.seekTo(0)
            } else {
                player.seekToPrevious()
            }
        }
    }

    private fun isSoughtToEnd(player: Player?, tolerance: Long = 1000L): Boolean {
        player?.let {
            val duration = it.duration
            val currentPosition = it.currentPosition
            return duration > 0 && currentPosition >= (duration - tolerance)
        }
        return false
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(tag, "onTaskRemoved")
        currentMediaItem?.let {
            coroutineScope.launch {
                Log.d(tag, "5")
                saveCurrentProgress(
                    currentMediaItem = it,
                    currentPosition = mediaSession?.player?.currentPosition ?: 0,
                    totalDuration = mediaSession?.player?.duration ?: 0
                )
            }
        }
        pauseAllPlayersAndStopSelf()
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
