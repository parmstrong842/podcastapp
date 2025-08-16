package com.example.podcastapp.data.local.newentities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episode_state",
    foreignKeys = [
        ForeignKey(
            entity = EpisodeEntity::class,
            parentColumns = ["episodeId"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["episodeId"], unique = true),
        // Unique across non-null queue positions (SQLite allows multiple NULLs)
        Index(value = ["queuePosition"], unique = true)
    ]
)
data class EpisodeStateEntity(
    @PrimaryKey val episodeId: Long,
    // progress
    val position: Long = 0L,
    val duration: Long = 0L,
    val finished: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    // history (present if not null)
    val lastPlayedAt: Long? = null,
    // queue (present if not null)
    val queuePosition: Int? = null,
    val enqueuedAt: Long? = null
)