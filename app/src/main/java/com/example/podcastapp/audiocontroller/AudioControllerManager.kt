package com.example.podcastapp.audiocontroller

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.example.podcastapp.ui.viewmodel.PodcastEpItem
import com.google.common.util.concurrent.MoreExecutors

private const val tag = "AudioControllerManager"

data class MediaInfo(
    val title: String,
    val episodeName: String,
    val imageUri: Uri?
)

object AudioControllerManager {
    private var mediaController: MediaController? = null

    fun initialize(context: Context) {
        if (mediaController == null) {
            val sessionToken = SessionToken(context, ComponentName(context, PodcastAudioService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener({
                mediaController = controllerFuture.get()

                mediaController?.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> Log.d(tag, "Player is ready")
                            Player.STATE_ENDED -> Log.d(tag, "Playback ended")
                            Player.STATE_BUFFERING -> Log.d(tag, "Buffering")
                            Player.STATE_IDLE -> Log.d(tag, "Player idle")
                        }
                    }
                })
            }, MoreExecutors.directExecutor())
        }
    }

    fun playMedia(pod: PodcastEpItem) {
        mediaController?.let { controller ->
            val mediaItem = MediaItem.Builder()
                .setUri(pod.enclosureUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(pod.episodeName)
                        .setArtworkUri(Uri.parse(pod.image))
                        .setArtist(pod.title)
                        .build()
                )
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    fun pauseMedia() {
        mediaController?.pause()
    }

    fun getCurrentMediaInfo(): MediaInfo? {
        val currentMediaItem = mediaController?.currentMediaItem ?: return null
        val metadata = currentMediaItem.mediaMetadata
        return MediaInfo(
            title = metadata.artist.toString(),
            episodeName = metadata.title.toString(),
            imageUri = metadata.artworkUri
        )
    }
}
