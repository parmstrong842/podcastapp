package com.example.podcastapp.audiocontroller

import android.os.Bundle
import androidx.media3.session.MediaController
import com.example.podcastapp.ui.viewmodel.PodcastEpItem
import kotlinx.coroutines.flow.StateFlow

interface IAudioControllerManager {
    val hasPlaylistItems: Boolean
    val isLoading: Boolean
    val shouldShowPlayButton: Boolean
    val sleepTimerActive: Boolean
    val currentMediaInfo: MediaInfo?
    val mediaIsPlaying: Boolean
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
    fun getCurrentPodcastId(): Int?
    fun getMediaController(): MediaController?
    fun release()
}