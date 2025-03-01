package com.example.podcastapp.data.remote.models.podcastindex

data class Podcast(
    val id: Int,
    val title: String,
    val description: String,
    val image: String?,
    val url: String,
    val author: String
)