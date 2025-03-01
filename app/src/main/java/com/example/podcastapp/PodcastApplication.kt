package com.example.podcastapp

import android.app.Application
import com.example.podcastapp.audiocontroller.AudioControllerManager
import com.example.podcastapp.data.AppDataContainer

class PodcastApplication : Application() {

    lateinit var container: AppDataContainer

    override fun onCreate() {
        super.onCreate()
        AudioControllerManager.initialize(this)
        container = AppDataContainer(this)
    }
}