package com.example.podcastapp.data.local.entities

import androidx.room.Entity

@Entity(tableName = "subscriptions", primaryKeys = ["feedUrl"])
data class SubscribedPodcastEntity(
    val title: String,
    val image: String?,
    val feedUrl: String
)
