package com.example.podcastapp.data.local.entities

import androidx.room.Entity

@Entity(tableName = "subscriptions", primaryKeys = ["id"])
data class SubscribedPodcastEntity(
    val id: Int,
    val title: String,
    val image: String?
)
