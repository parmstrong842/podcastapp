package com.example.podcastapp.audiocontroller

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.session.MediaController
import com.example.podcastapp.ui.viewmodel.PodcastEpItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AudioControllerManagerMock(
    override val hasPlaylistItems: Boolean = true,
    override val isLoading: Boolean = false,
    override val shouldShowPlayButton: Boolean = true,
    override val sleepTimerActive: Boolean = false,
    override val currentMediaInfo: MediaInfo? = MediaInfo(
        title = "Fall of Civilizations Podcast",
        episodeName = "4. The Greenland Vikings - Land of the Midnight Sun",
        imageUri = "https://d3t3ozftmdmh3i.cloudfront.net/staging/podcast_uploaded_episode/43131353/3192d515c95fdf40.jpg".toUri()
    ),
    override val mediaIsPlaying: Boolean = true,
    override val currentSpeed: StateFlow<String> = MutableStateFlow("1x"),
    val sliderCurrentProgress: Float = 0.25f,
    val sliderBufferProgress: Float = 0.5f,
    val currentPodcastId: Int = 1,
) : IAudioControllerManager {
    override fun initializeMediaController() {}

    override fun sleepTimer(durationMillis: Long) {}

    override fun cancelSleepTimer() {}

    override fun getProgress(): Float {
        return sliderCurrentProgress
    }

    override fun getBufferProgress(): Float {
        return sliderBufferProgress
    }

    override fun seekToProgress(progress: Float) {}

    override fun playMedia(pod: PodcastEpItem) {}

    override fun pauseMedia() {}

    override fun resumePlayback() {}

    override fun sendCustomCommand(action: String, extras: Bundle) {}

    override fun getCurrentPodcastId(): Int? {
        return currentPodcastId
    }

    override fun getMediaController(): MediaController? {
        return null
    }

    override fun release() {}
}