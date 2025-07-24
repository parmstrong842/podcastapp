package com.example.podcastapp.data.local.entities

import androidx.room.Entity

@Entity(tableName = "episode_history", primaryKeys = ["episodeId"])
data class EpisodeHistoryEntity(
    val image: String,
    val title: String,
    val datePublishedPretty: String,
    val datePublished: Long,
    val episodeName: String,
    val description: String,
    val enclosureUrl: String,
    val timeLeft: String,
    val progress: Float,
    val podcastId: Int,
    val episodeId: Long,
    val played: Boolean,
    val playedAtMillis: Long
)
