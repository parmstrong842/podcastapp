package com.example.podcastapp.data.local.newentities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = ["feedUrl"],
            childColumns = ["feedUrl"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("feedUrl"),
        Index(value = ["feedUrl", "guid"], unique = true)
    ]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val episodeId: Long = 0L,
    val feedUrl: String,
    val guid: Long,
    val episodeName: String,
    val description: String,
    val enclosureUrl: String,
    val image: String,
    val pubDate: String
)