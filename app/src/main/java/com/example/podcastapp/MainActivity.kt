package com.example.podcastapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.podcastapp.audiocontroller.AudioControllerManagerImpl
import com.example.podcastapp.audiocontroller.IAudioControllerManager
import com.example.podcastapp.ui.theme.PodcastAppTheme

private const val tag = "MyMainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var audioControllerManager: IAudioControllerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("media_prefs", MODE_PRIVATE)
        audioControllerManager = AudioControllerManagerImpl(this, (application as PodcastApplication).container.databaseRepository, sharedPrefs)

        setContent {
            PodcastAppTheme {
                PodcastNavGraph(audioControllerManager)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audioControllerManager.initializeMediaController()
    }

    override fun onStop() {
        Log.d(tag, "onStop")
        super.onStop()
        if (!audioControllerManager.mediaIsPlaying) {
            audioControllerManager.release()
        }
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        super.onDestroy()
        audioControllerManager.release()
    }
}

/*
the service needs to be destroyed app is dismissed from the task stack
sleep timer needs to work when app is in background
sleep timer needs media controller to send pause command
if media is not playing and app goes to background service needs to be destroyed
 */