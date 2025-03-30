package com.example.podcastapp.audiocontroller

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class AudioFocusHelper(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Listener to detect audio focus changes
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Audio focus has been regained: resume playback if needed
                resumePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost audio focus: pause playback
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Optionally, lower the volume if ducking is supported
                duckAudio()
            }
        }
    }

    // Request audio focus for playback
    fun requestAudioFocus(): Boolean {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
        return audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    // Abandon audio focus when it's no longer needed
    fun abandonAudioFocus() {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    // Pause playback in your app (e.g., pause your MediaPlayer or audio session)
    private fun pausePlayback() {
        // Insert your pause logic here, e.g., mediaPlayer.pause()
    }

    // Resume playback if appropriate
    private fun resumePlayback() {
        // Insert your resume logic here, e.g., mediaPlayer.start()
    }

    // Optionally, handle ducking (lowering volume) when necessary
    private fun duckAudio() {
        // Insert your ducking logic here (e.g., lower the volume)
    }
}
