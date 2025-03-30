package com.example.podcastapp.data.remote.models.podcastindex

data class EpisodeResponse(
    val status: String,
    val id: String,
    val episode: Episode,
    val description: String
)
