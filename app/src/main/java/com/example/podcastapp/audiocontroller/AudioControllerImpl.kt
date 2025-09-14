package com.example.podcastapp.audiocontroller

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.ui.components.PodcastEpItem
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.core.content.edit
import androidx.media3.common.C
import com.example.podcastapp.data.local.model.EpisodeProgress
import com.google.gson.Gson
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

private const val tag = "AudioController"

data class MediaInfo(
    val podcastTitle: String,
    val episodeTitle: String,
    val episodeImage: Uri?
)

data class EpisodeMetadata(
    val podcastTitle: String,
    val podcastImage: String,
    val pubDate: String,
    val episodeTitle: String,
    val episodeImage: String,
    val episodeDescription: String,
    val enclosureUrl: String,
    val feedUrl: String,
    val guid: String,
)

class AudioControllerImpl(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val sharedPrefs: SharedPreferences,
    private val gson: Gson,
) : IAudioController {

    override var hasPlaylistItems by mutableStateOf(false)
        private set
    override var isLoading by mutableStateOf(false)
        private set
    override var isPlaying by mutableStateOf(false)
        private set
    override var nowPlayingGuid by mutableStateOf<String?>(null)
        private set
    override var shouldShowPlayButton by mutableStateOf(true)
        private set
    override var sleepTimerActive by mutableStateOf(false)
        private set
    override var currentMediaInfo by mutableStateOf<MediaInfo?>(null)
        private set

    private var mediaController: MediaController? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var playMediaJob: Job? = null
    private var timerJob: Job? = null
    private var progressUpdateJob: Job? = null

    private val _currentSpeed = MutableStateFlow("1x")
    override val currentSpeed: StateFlow<String> = _currentSpeed.asStateFlow()

    private var currentEpisodeMetadata: EpisodeMetadata? = null

    override fun initializeMediaController() {
        if (mediaController == null) {
            val sessionToken =
                SessionToken(context, ComponentName(context, PodcastAudioService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener({
                mediaController = controllerFuture.get()
                fetchSavedMediaItem(sharedPrefs)

                mediaController?.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                Log.d(tag, "Player is ready")
                                if (mediaController?.playWhenReady == true) {
                                    scope.launch {
                                        currentEpisodeMetadata?.let {
                                            databaseRepository.insertEpisodeHistory(
                                                metadata = it,
                                                duration = mediaController?.duration ?: 0
                                            )
                                        }
                                    }
                                }
                                isLoading = false
                            }

                            Player.STATE_ENDED -> {
                                Log.d(tag, "Playback ended")
                                isLoading = false
                                currentEpisodeMetadata?.let {
                                    scope.launch {
                                        saveCurrentProgress(
                                            feedUrl = it.feedUrl,
                                            guid = it.guid,
                                            currentPosition = mediaController?.currentPosition ?: 0,
                                            totalDuration = mediaController?.duration ?: 0,
                                            reason = "playback ended",
                                            finished = true
                                        )
                                    }
                                }
                            }

                            Player.STATE_BUFFERING -> {
                                Log.d(tag, "Buffering")
                                isLoading = true
                            }

                            Player.STATE_IDLE -> {
                                Log.d(tag, "Player idle")
                                isLoading = false
                            }
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        //Log.d(tag, "onMediaItemTransition")
                        nowPlayingGuid = currentEpisodeMetadata?.guid
                        updatePlaylistState()
                        currentMediaInfo = mediaItem?.let { item ->
                            val metadata = item.mediaMetadata
                            MediaInfo(
                                podcastTitle = metadata.artist.toString(),
                                episodeTitle = metadata.title.toString(),
                                episodeImage = metadata.artworkUri
                            )
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(tag, "isPlaying: $isPlaying")
                        this@AudioControllerImpl.isPlaying = isPlaying
                        updatePlayButtonState()
                        if (isPlaying) {
                            // backup in case other methods fail to save progress
                            if (progressUpdateJob == null || progressUpdateJob?.isCancelled == true) {
                                progressUpdateJob = scope.launch {
                                    while (isActive) {
                                        currentEpisodeMetadata?.let {
                                            scope.launch {
                                                saveCurrentProgress(
                                                    feedUrl = it.feedUrl,
                                                    guid = it.guid,
                                                    currentPosition = mediaController?.currentPosition ?: 0,
                                                    totalDuration = mediaController?.duration ?: 0,
                                                    reason = "periodic or playback started"
                                                )
                                            }
                                        }
                                        delay(60000)
                                    }
                                }
                            }
                        } else {
                            progressUpdateJob?.cancel()
                            progressUpdateJob = null
                            if (mediaController?.playbackState != Player.STATE_ENDED) {
                                currentEpisodeMetadata?.let {
                                    scope.launch {
                                        saveCurrentProgress(
                                            feedUrl = it.feedUrl,
                                            guid = it.guid,
                                            currentPosition = mediaController?.currentPosition ?: 0,
                                            totalDuration = mediaController?.duration ?: 0,
                                            reason = "playback stopped but not ended"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        if (reason == Player.DISCONTINUITY_REASON_REMOVE || reason == Player.DISCONTINUITY_REASON_SEEK) {
                            currentEpisodeMetadata?.let {
                                scope.launch {
                                    saveCurrentProgress(
                                        feedUrl = it.feedUrl,
                                        guid = it.guid,
                                        currentPosition = oldPosition.positionMs,
                                        totalDuration = mediaController?.duration ?: 0,
                                        reason = "position discontinuity $reason"
                                    )
                                }
                            }
                        }
                    }

                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        val speedText = when (playbackParameters.speed) {
                            0.5f -> "0.5x"
                            0.7f -> "0.7x"
                            1.0f -> "1x"
                            1.2f -> "1.2x"
                            1.5f -> "1.5x"
                            1.7f -> "1.7x"
                            2.0f -> "2x"
                            else -> "1x"
                        }
                        _currentSpeed.value = speedText
                    }
                })

                updatePlaylistState()
            }, MoreExecutors.directExecutor())
        }
    }

    override fun playMedia(pod: PodcastEpItem) {
        playMediaJob = scope.launch {
            val mediaItemToSave = EpisodeMetadata(
                podcastTitle = pod.podcastTitle,
                podcastImage = pod.podcastImage,
                pubDate = pod.pubDate,
                episodeTitle = pod.episodeTitle,
                episodeImage = pod.episodeImage,
                episodeDescription = pod.episodeDescription,
                enclosureUrl = pod.enclosureUrl,
                feedUrl = pod.feedUrl,
                guid = pod.guid,
            )
            currentEpisodeMetadata = mediaItemToSave
            saveCurrentMediaItem(mediaItemToSave)
            nowPlayingGuid = pod.guid

            val savedProgress = databaseRepository.getProgress(pod.feedUrl, pod.guid)
            prepareMediaItem(pod.enclosureUrl, pod.episodeTitle, pod.episodeImage, pod.podcastTitle, savedProgress)

            mediaController?.play()
        }
    }

    private fun prepareMediaItem(
        enclosure: String,
        episodeTitle: String,
        episodeImage: String,
        podcastTitle: String,
        savedProgress: EpisodeProgress?
    ) {
        mediaController?.let { controller ->
            val mediaItem = MediaItem.Builder()
                .setUri(enclosure)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(episodeTitle)
                        .setArtworkUri(episodeImage.toUri())
                        .setArtist(podcastTitle)
                        .build()
                )
                .build()

            if (savedProgress != null && !savedProgress.finished) {
                controller.setMediaItem(mediaItem, savedProgress.position)
            } else {
                controller.setMediaItem(mediaItem)
            }
            controller.prepare()
        }
    }

    private suspend fun saveCurrentProgress(
        feedUrl: String,
        guid: String,
        currentPosition: Long,
        totalDuration: Long,
        reason: String = "",
        finished: Boolean = false
    ) {
        if (totalDuration == C.TIME_UNSET) {
            Log.d(tag, "save: $reason -- invalid duration, skipping save.")
            return
        }

        Log.d(tag, "save: $reason")

        try {
            withContext(Dispatchers.IO) {
                databaseRepository.saveProgress(
                    feedUrl = feedUrl,
                    guid = guid,
                    position = currentPosition,
                    duration = totalDuration,
                    finished = finished
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to save progress", e)
        }
    }


    private fun updatePlaylistState() {
        hasPlaylistItems = (mediaController?.mediaItemCount ?: 0) > 0
    }

    private fun updatePlayButtonState() {
        mediaController?.let { player ->
            shouldShowPlayButton = Util.shouldShowPlayButton(player)
        } ?: run {
            shouldShowPlayButton = true
        }
    }

    override fun sleepTimer(durationMillis: Long) {
        cancelSleepTimer()
        timerJob = scope.launch {
            sleepTimerActive = true
            delay(durationMillis)
            mediaController?.pause()
            sleepTimerActive = false
        }
    }

    override fun cancelSleepTimer() {
        sleepTimerActive = false
        timerJob?.cancel()
        timerJob = null
    }

    override fun getProgress(): Float {
        mediaController?.let { player ->
            val duration = player.duration
            // Return 0 if the duration is not known or invalid
            if (duration <= 0L) return 0f
            return (player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        }
        return 0f
    }

    override fun getBufferProgress(): Float {
        mediaController?.let { player ->
            val duration = player.duration
            // Return 0 if the duration is not known or invalid
            if (duration <= 0L) return 0f
            return (player.bufferedPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        }
        return 0f
    }

    override fun seekToProgress(progress: Float) {
        mediaController?.let { player ->
            val duration = player.duration
            if (duration > 0) {
                val newPosition = (progress.coerceIn(0f, 1f) * duration).toLong()
                player.seekTo(newPosition)
            }
        }
    }

    override fun pauseMedia() {
        mediaController?.pause()
    }

    override fun resumePlayback() {
        mediaController?.play()
    }

    override fun sendCustomCommand(action: String, extras: Bundle) {
        val controller = mediaController ?: return
        val sessionCommand = SessionCommand(action, extras)
        controller.sendCustomCommand(sessionCommand, extras)
            .addListener(
                {
                    Log.d(tag, "Custom command sent: $action")
                },
                MoreExecutors.directExecutor()
            )
    }

    private fun saveCurrentMediaItem(metadata: EpisodeMetadata) {
        val json = gson.toJson(metadata)
        sharedPrefs.edit { putString("last_media_item", json) }
    }

    private fun fetchSavedMediaItem(sharedPrefs: SharedPreferences) {
        scope.launch {
            val jsonString = sharedPrefs.getString("last_media_item", null)
            jsonString?.let {
                val metadata = gson.fromJson(it, EpisodeMetadata::class.java)
                currentEpisodeMetadata = metadata
                val savedProgress = databaseRepository.getProgress(metadata.feedUrl, metadata.guid)

                prepareMediaItem(metadata.enclosureUrl, metadata.episodeTitle, metadata.episodeImage, metadata.podcastTitle, savedProgress)
            }
        }
    }

    override fun getCurrentPodcastFeedUrl(): String? {
        return currentEpisodeMetadata?.feedUrl
    }

    override fun getMediaController(): MediaController? = mediaController

    override fun release() {
        Log.d(tag, "release")
        playMediaJob?.cancel()
        playMediaJob = null
        progressUpdateJob?.cancel()
        progressUpdateJob = null

        val currentPosition = mediaController?.currentPosition ?: 0
        val totalDuration = mediaController?.duration ?: 0
        currentEpisodeMetadata?.let {
            scope.launch {
                saveCurrentProgress(
                    feedUrl = it.feedUrl,
                    guid = it.guid,
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,
                    reason = "release called"
                )
            }
        }

        mediaController?.release()
        mediaController = null
    }
}