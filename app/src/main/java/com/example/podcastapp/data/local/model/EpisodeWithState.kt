package com.example.podcastapp.data.local.model

data class EpisodeWithState(
    val podcastTitle: String,
    val podcastImage: String,
    val pubDate: String,
    val episodeTitle: String,
    val episodeImage: String,
    val episodeDescription: String,
    val enclosureUrl: String,
    val duration: Long,
    val position: Long,
    val feedUrl: String,
    val guid: String,
    val finished: Boolean,
)
