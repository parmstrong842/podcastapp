package com.example.podcastapp.audiocontroller

import android.os.Bundle
import androidx.media3.session.MediaController
import com.example.podcastapp.ui.components.PodcastEpItem
import kotlinx.coroutines.flow.StateFlow

interface IAudioController {
    val hasPlaylistItems: Boolean
    val isLoading: Boolean
    val isPlaying: Boolean
    val nowPlayingGuid: String?
    val shouldShowPlayButton: Boolean
    val sleepTimerActive: Boolean
    val currentMediaInfo: MediaInfo?
    val currentSpeed: StateFlow<String>

    fun initializeMediaController()
    fun sleepTimer(durationMillis: Long)
    fun cancelSleepTimer()
    fun getProgress(): Float
    fun getBufferProgress(): Float
    fun seekToProgress(progress: Float)
    fun playMedia(pod: PodcastEpItem)
    fun pauseMedia()
    fun resumePlayback()
    fun sendCustomCommand(action: String, extras: Bundle)
    fun getCurrentPodcastFeedUrl(): String?
    fun getMediaController(): MediaController?
    fun release()
}