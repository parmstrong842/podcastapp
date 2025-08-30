package com.example.podcastapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcasts")
data class PodcastEntity(
    @PrimaryKey val feedUrl: String,
    val podcastTitle: String,
    val podcastImage: String?,
    val subscribed: Boolean = false
)