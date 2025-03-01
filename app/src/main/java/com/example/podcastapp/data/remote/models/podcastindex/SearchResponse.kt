package com.example.podcastapp.data.remote.models.podcastindex

data class SearchResponse(
    val status: String,
    val feeds: List<Podcast>,
    val count: Int,
    val query: String,
    val description: String
)