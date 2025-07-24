package com.example.podcastapp.data.remote.models.podcastindex

data class Episode(
    val id: Long,
    val title: String,
    val description: String,
    val enclosureUrl: String,
    val datePublished: Long,
    val datePublishedPretty: String,
    val image: String,
    val duration: Int
)