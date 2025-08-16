package com.example.podcastapp.data.local

import com.example.podcastapp.data.local.entities.EpisodeHistoryEntity
import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.example.podcastapp.data.local.entities.QueueEntity
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import com.example.podcastapp.ui.components.PodcastEpItem
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {

    fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcastEntity>>

    suspend fun getSubscription(feedUrl: String): SubscribedPodcastEntity?

    suspend fun insertSubscription(item: SubscribedPodcastEntity)

    suspend fun updateSubscription(item: SubscribedPodcastEntity)

    suspend fun deleteSubscription(item: SubscribedPodcastEntity)


    suspend fun saveProgress(progress: PodcastProgressEntity)

    suspend fun getProgress(feedUrl: String, guid: Long): PodcastProgressEntity?

    suspend fun getAllProgressForPodcast(feedUrl: String): List<PodcastProgressEntity>

    suspend fun getRecentProgress(): List<PodcastProgressEntity>


    fun getHistoryFlow(): Flow<List<EpisodeHistoryEntity>>

    suspend fun insertEpisodeHistory(history: EpisodeHistoryEntity)


    fun getQueueFlow(): Flow<List<QueueEntity>>

    suspend fun enqueue(podcastEpItem: PodcastEpItem)

    suspend fun remove(key: String)

    suspend fun move(key: String, newIndex: Int)
}