package com.example.podcastapp.audiocontroller


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.podcastapp.R
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlin.math.min


private const val tag = "PodcastAudioService"

class PodcastAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    private val SEEK_BACK = "SEEK_BACK"
    private val SEEK_FORWARD = "SEEK_FORWARD"
    private val SPEED_0_5X = "SPEED_0_5X"
    private val SPEED_0_7X = "SPEED_0_7X"
    private val SPEED_1X = "SPEED_1X"
    private val SPEED_1_2X = "SPEED_1_2X"
    private val SPEED_1_5X = "SPEED_1_5X"
    private val SPEED_1_7X = "SPEED_1_7X"
    private val SPEED_2X = "SPEED_2X"

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

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(CustomMediaSessionCallback())
            .setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1xButton))
            .build()
    }

    private inner class CustomMediaSessionCallback: MediaSession.Callback {
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
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_0_7xButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_0_7X -> {
                    session.player.playbackParameters = PlaybackParameters(1f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1xButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1X -> {
                    session.player.playbackParameters = PlaybackParameters(1.2f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1_2xButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1_2X -> {
                    session.player.playbackParameters = PlaybackParameters(1.5f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1_5xButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1_5X -> {
                    session.player.playbackParameters = PlaybackParameters(1.7f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_1_7xButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_1_7X -> {
                    session.player.playbackParameters = PlaybackParameters(2f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_2xButton))
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                SPEED_2X -> {
                    session.player.playbackParameters = PlaybackParameters(0.5f, 1.0f)
                    session.setCustomLayout(ImmutableList.of(seekBackButton, seekForwardButton, speed_0_5xButton))
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
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED) {
            // Stop the service if not playing, continue playing in the background otherwise.
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
