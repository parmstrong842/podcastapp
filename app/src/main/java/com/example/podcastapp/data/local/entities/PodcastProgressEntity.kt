package com.example.podcastapp.data.local.entities

import androidx.room.Entity

@Entity(tableName = "podcast_progress", primaryKeys = ["podcastId", "episodeId"])
data class PodcastProgressEntity(
    val podcastId: Int,
    val episodeId: Long,
    val position: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val finished: Boolean = false
)
