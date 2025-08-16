package com.example.podcastapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "episode_queue")
data class QueueEntity(
    @PrimaryKey val key: String,
    val image: String,
    val podcastTitle: String,
    val pubDate: String,
    val episodeName: String,
    val description: String,
    val enclosureUrl: String,
    val timeLeft: String,
    val progress: Float,
    val feedUrl: String,
    val guid: Long,
    val played: Boolean,
    val enqueued: Boolean,
    val queuePosition: Int,
)