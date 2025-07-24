package com.example.podcastapp.data.remote.models.podcastindex

data class PodcastByFeedIDResponse(
    val status: String,
    val query: Query,
    val feed: Feed,
    val description: String
)
