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
import com.example.podcastapp.data.local.model.EpisodeProgress

private const val tag = "AudioControllerManager"

data class MediaInfo(
    val title: String,
    val episodeName: String,
    val imageUri: Uri?
)

data class SavedMediaItem(
    val feedUrl: String,
    val guid: String,
    val enclosureUri: String,
    val episodeName: String,
    val image: String,
    val title: String,
)

class AudioControllerManagerImpl(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val sharedPrefs: SharedPreferences
) : IAudioControllerManager {

    override var hasPlaylistItems by mutableStateOf(false)
        private set
    override var isLoading by mutableStateOf(false)
        private set
    override var shouldShowPlayButton by mutableStateOf(true)
        private set
    override var sleepTimerActive by mutableStateOf(false)
        private set
    override var currentMediaInfo by mutableStateOf<MediaInfo?>(null)
        private set
    override var mediaIsPlaying by mutableStateOf(false)
        private set

    private var mediaController: MediaController? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var playMediaJob: Job? = null
    private var timerJob: Job? = null

    private val _currentSpeed = MutableStateFlow("1x")
    override val currentSpeed: StateFlow<String> = _currentSpeed.asStateFlow()

    override fun initializeMediaController() {
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
        episodeTitle: String,
        episodeImage: String,
        podcastTitle: String,
        extras: Bundle,
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

    override fun playMedia(pod: PodcastEpItem) {
        playMediaJob = scope.launch {
            val extras = Bundle().apply {
                putString("FEED_URL", pod.feedUrl)
                putString("GUID", pod.guid)
            }

            val mediaItemToSave = SavedMediaItem(
                feedUrl = pod.feedUrl,
                guid = pod.guid,
                enclosureUri = pod.enclosureUrl,
                episodeName = pod.episodeTitle,
                image = pod.episodeImage,
                title = pod.podcastTitle,
            )
            saveCurrentMediaItem(mediaItemToSave)

            val savedProgress = databaseRepository.getProgress(pod.feedUrl, pod.guid)
            prepareMediaItem(pod.enclosureUrl, pod.episodeTitle, pod.episodeImage, pod.podcastTitle, extras, savedProgress)
            databaseRepository.insertEpisodeHistory(pod)

            mediaController?.play()
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

    private fun saveCurrentMediaItem(pod: SavedMediaItem) {
        sharedPrefs.edit {
            putString("current_media_item_feed_url", pod.feedUrl)
            putString("current_media_item_guid", pod.guid)
            putString("current_media_item_enclosure", pod.enclosureUri)
            putString("current_media_item_episode_name", pod.episodeName)
            putString("current_media_item_image", pod.image)
            putString("current_media_item_title", pod.title)
        }
    }

    private fun fetchCurrentMediaItem(sharedPrefs: SharedPreferences) {
        scope.launch {
            if (!sharedPrefs.contains("current_media_item_guid")) {
                return@launch
            }

            val feedUrl = sharedPrefs.getString("current_media_item_feed_url", "") ?: ""
            val guid = sharedPrefs.getString("current_media_item_guid", "")
            val enclosure = sharedPrefs.getString("current_media_item_enclosure", "") ?: ""
            val episodeName = sharedPrefs.getString("current_media_item_episode_name", "") ?: ""
            val image = sharedPrefs.getString("current_media_item_image", "") ?: ""
            val title = sharedPrefs.getString("current_media_item_title", "") ?: ""

            val extras = Bundle().apply {
                putString("FEED_URL", feedUrl)
                putString("GUID", guid)
            }

//            val savedProgress = databaseRepository.getProgress(feedUrl, guid)
//
//            prepareMediaItem(enclosure, episodeName, image, title, extras, savedProgress)
        }
    }

    override fun getCurrentPodcastFeedUrl(): String? {
        return mediaController?.currentMediaItem?.mediaMetadata?.extras?.getString("FEED_URL")
    }

    override fun getMediaController(): MediaController? = mediaController

    override fun release() {
        Log.d(tag, "release")
        playMediaJob?.cancel()
        playMediaJob = null
        mediaController?.release()
        mediaController = null
    }
}
