package com.example.podcastapp

import android.app.Application
import com.example.podcastapp.data.AppDataContainer

// TODO: detect and pause when headphones disconnect
// TODO: notification play button out of sync with player; pass button state when creating view
// TODO: make sure app works in landscape mode
// TODO: if app is in background and is paused the service gets killed after a while; don't lose current media item
// TODO: bug with saveProgress. if I pause the media and seek then put app in background it doesn't save the progress
// TODO: add private rss feeds
// TODO: chop off url parameters from feed url when subscribing

class PodcastApplication : Application() {

    lateinit var container: AppDataContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}