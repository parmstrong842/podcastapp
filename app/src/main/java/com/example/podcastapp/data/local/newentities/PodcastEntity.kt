package com.example.podcastapp.data.local.newentities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcasts")
data class PodcastEntity(
    @PrimaryKey val feedUrl: String,
    val title: String,
    val image: String?,
    val subscribed: Boolean = false
)