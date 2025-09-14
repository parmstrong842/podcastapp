package com.example.podcastapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.podcastapp.audiocontroller.AudioControllerImpl
import com.example.podcastapp.audiocontroller.IAudioController
import com.example.podcastapp.ui.theme.PodcastAppTheme
import com.google.gson.Gson

private const val tag = "MyMainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var audioController: IAudioController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("media_prefs", MODE_PRIVATE)
        audioController = AudioControllerImpl(this, (application as PodcastApplication).container.databaseRepository, sharedPrefs, Gson())

        setContent {
            PodcastAppTheme {
                PodcastNavGraph(audioController)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audioController.initializeMediaController()
    }

    override fun onStop() {
        Log.d(tag, "onStop")
        super.onStop()
        if (!audioController.isPlaying) {
            audioController.release()
        }
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        super.onDestroy()
        audioController.release()
    }
}

/*
the service needs to be destroyed app is dismissed from the task stack
sleep timer needs to work when app is in background
sleep timer needs media controller to send pause command
if media is not playing and app goes to background service needs to be destroyed
 */