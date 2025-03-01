package com.example.podcastapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions", primaryKeys = ["id"])
data class SubscribedPodcast(
    val id: Int,
    val title: String,
    val image: String?
)
