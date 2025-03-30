package com.example.podcastapp.data.remote.models.podcastindex

data class EpisodeFeedResponse(
    val status: String,
    val items: List<Episode>,
    val count: Int,
    val query: String,
    val description: String
)