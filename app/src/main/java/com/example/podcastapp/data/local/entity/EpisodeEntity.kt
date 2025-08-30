package com.example.podcastapp.data.local.entity

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
    val guid: String,
    val episodeTitle: String,
    val episodeImage: String,
    val episodeDescription: String,
    val enclosureUrl: String,
    val pubDate: String
)