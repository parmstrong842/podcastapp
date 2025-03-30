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
import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.example.podcastapp.ui.viewmodel.PodcastEpItem
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

private const val tag = "AudioControllerManager"

data class MediaInfo(
    val title: String,
    val episodeName: String,
    val imageUri: Uri?
)

data class SavedMediaItem(
    val podcastId: Int,
    val episodeId: Long,
    val enclosureUri: String,
    val episodeName: String,
    val image: String,
    val title: String,
)

class AudioControllerManager(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val sharedPrefs: SharedPreferences
) {
    private var mediaController: MediaController? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var playMediaJob: Job? = null
    private var timerJob: Job? = null

    var hasPlaylistItems by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var shouldShowPlayButton by mutableStateOf(true)
        private set
    var sleepTimerActive by mutableStateOf(false)
        private set
    var currentMediaInfo by mutableStateOf<MediaInfo?>(null)
        private set
    var mediaIsPlaying by mutableStateOf(false)
        private set

    private val _currentSpeed = MutableStateFlow("1x")
    val currentSpeed: StateFlow<String> = _currentSpeed.asStateFlow()

    fun initializeMediaController() {
        if (mediaController == null) {
            val sessionToken =
                SessionToken(context, ComponentName(context, PodcastAudioService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener({
                mediaController = controllerFuture.get()
                fetchCurrentMediaItem(sharedPrefs)

                mediaController?.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                Log.d(tag, "Player is ready")
                                isLoading = false
                            }

                            Player.STATE_ENDED -> {
                                Log.d(tag, "Playback ended")
                                isLoading = false
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
                        Log.d(tag, "onMediaItemTransition")
                        updatePlaylistState()
                        currentMediaInfo = mediaItem?.let { item ->
                            val metadata = item.mediaMetadata
                            MediaInfo(
                                title = metadata.artist.toString(),
                                episodeName = metadata.title.toString(),
                                imageUri = metadata.artworkUri
                            )
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(tag, "isPlaying: $isPlaying")
                        mediaIsPlaying = isPlaying
                        updatePlayButtonState()
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

    private fun prepareMediaItem(
        enclosure: String,
        episodeName: String,
        image: String,
        title: String,
        extras: Bundle,
        savedProgress: PodcastProgressEntity?
    ) {
        mediaController?.let { controller ->
            val mediaItem = MediaItem.Builder()
                .setUri(enclosure)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(episodeName)
                        .setArtworkUri(Uri.parse(image))
                        .setArtist(title)
                        .setExtras(extras)
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


    fun sleepTimer(durationMillis: Long) {
        cancelSleepTimer()
        timerJob = scope.launch {
            sleepTimerActive = true
            delay(durationMillis)
            mediaController?.pause()
            sleepTimerActive = false
        }
    }

    fun cancelSleepTimer() {
        sleepTimerActive = false
        timerJob?.cancel()
        timerJob = null
    }

    fun getProgress(): Float {
        mediaController?.let { player ->
            val duration = player.duration
            // Return 0 if the duration is not known or invalid
            if (duration <= 0L) return 0f
            return player.currentPosition.toFloat() / duration.toFloat()
        }
        return 0f
    }

    fun seekToProgress(progress: Float) {
        mediaController?.let { player ->
            val duration = player.duration
            if (duration > 0) {
                val newPosition = (progress.coerceIn(0f, 1f) * duration).toLong()
                player.seekTo(newPosition)
            }
        }
    }

    fun playMedia(pod: PodcastEpItem) {
        playMediaJob = scope.launch {
            val extras = Bundle().apply {
                putInt("PODCAST_ID", pod.podcastId)
                putLong("EPISODE_ID", pod.episodeId)
            }

            val mediaItemToSave = SavedMediaItem(
                podcastId = pod.podcastId,
                episodeId = pod.episodeId,
                enclosureUri = pod.enclosureUrl,
                episodeName = pod.episodeName,
                image = pod.image,
                title = pod.title,
            )
            saveCurrentMediaItem(mediaItemToSave)

            val savedProgress = databaseRepository.getProgress(pod.podcastId, pod.episodeId)

            prepareMediaItem(pod.enclosureUrl, pod.episodeName, pod.image, pod.title, extras, savedProgress)
            mediaController?.play()
        }
    }

    fun pauseMedia() {
        mediaController?.pause()
    }

    fun resumePlayback() {
        mediaController?.play()
    }

    fun sendCustomCommand(action: String, extras: Bundle) {
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

    private fun saveCurrentMediaItem(pod: SavedMediaItem) {
        with(sharedPrefs.edit()) {
            this?.putInt("current_media_item_podcast_id", pod.podcastId)
            this?.putLong("current_media_item_episode_id", pod.episodeId)
            this?.putString("current_media_item_enclosure", pod.enclosureUri)
            this?.putString("current_media_item_episode_name", pod.episodeName)
            this?.putString("current_media_item_image", pod.image)
            this?.putString("current_media_item_title", pod.title)
            this?.apply()
        }
    }

    private fun fetchCurrentMediaItem(sharedPrefs: SharedPreferences) {
        scope.launch {
            if (!sharedPrefs.contains("current_media_item_episode_id")) {
                return@launch
            }

            val podcastId = sharedPrefs.getInt("current_media_item_podcast_id", -1)
            val episodeId = sharedPrefs.getLong("current_media_item_episode_id", -1L)
            val enclosure = sharedPrefs.getString("current_media_item_enclosure", "") ?: ""
            val episodeName = sharedPrefs.getString("current_media_item_episode_name", "") ?: ""
            val image = sharedPrefs.getString("current_media_item_image", "") ?: ""
            val title = sharedPrefs.getString("current_media_item_title", "") ?: ""

            val extras = Bundle().apply {
                putInt("PODCAST_ID", podcastId)
                putLong("EPISODE_ID", episodeId)
            }

            val savedProgress = databaseRepository.getProgress(podcastId, episodeId)

            prepareMediaItem(enclosure, episodeName, image, title, extras, savedProgress)
        }
    }

    fun getMediaController(): MediaController? = mediaController

    fun release() {
        Log.d(tag, "release")
        playMediaJob?.cancel()
        playMediaJob = null
        mediaController?.release()
        mediaController = null
    }
}
