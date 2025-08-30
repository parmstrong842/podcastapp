package com.example.podcastapp.data.local.model

data class EpisodeProgress(
    val position: Long,
    val duration: Long,
    val finished: Boolean,
)