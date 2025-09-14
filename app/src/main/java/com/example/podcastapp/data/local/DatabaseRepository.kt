package com.example.podcastapp.data.local

import com.example.podcastapp.audiocontroller.EpisodeMetadata
import com.example.podcastapp.data.local.entity.PodcastEntity
import com.example.podcastapp.data.local.model.EpisodeProgress
import com.example.podcastapp.data.local.model.EpisodeWithState
import com.example.podcastapp.ui.components.PodcastEpItem
import kotlinx.coroutines.flow.Flow


interface DatabaseRepository {
    suspend fun subscribe(feedUrl: String, podcastTitle: String, image: String?)

    suspend fun unsubscribe(feedUrl: String)

    fun getAllSubscriptionsFlow(): Flow<List<PodcastEntity>>

    suspend fun isSubscribed(feedUrl: String): Boolean?

    suspend fun saveProgress(feedUrl: String, guid: String, position: Long, duration: Long, finished: Boolean)

    suspend fun getProgress(feedUrl: String, guid: String): EpisodeProgress?

    suspend fun getAllProgressForPodcast(feedUrl: String): List<PodcastEpItem>

    suspend fun insertEpisodeHistory(metadata: EpisodeMetadata, duration: Long)

    fun getHistoryFlow(): Flow<List<PodcastEpItem>>
}
