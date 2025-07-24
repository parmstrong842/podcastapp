package com.example.podcastapp.data.local.entities

import androidx.room.Entity

@Entity(tableName = "podcast_progress", primaryKeys = ["podcastId", "episodeId"])
data class PodcastProgressEntity(
    val podcastId: Int,
    val episodeId: Long,
    val position: Long,
    val duration: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val finished: Boolean = false
)

fun PodcastProgressEntity.timeLeftMs(): Long =
    (duration - position).coerceAtLeast(0L)

fun PodcastProgressEntity.progressFraction(): Float =
    if (duration > 0L)
        (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    else 0f