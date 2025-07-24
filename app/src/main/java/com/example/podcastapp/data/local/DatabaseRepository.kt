package com.example.podcastapp.data.local

import com.example.podcastapp.data.local.entities.EpisodeHistoryEntity
import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {

    fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcastEntity>>

    suspend fun getSubscription(id: Int): SubscribedPodcastEntity?

    suspend fun insertSubscription(item: SubscribedPodcastEntity)

    suspend fun updateSubscription(item: SubscribedPodcastEntity)

    suspend fun deleteSubscription(item: SubscribedPodcastEntity)


    suspend fun saveProgress(progress: PodcastProgressEntity)

    suspend fun getProgress(podcastId: Int, episodeId: Long): PodcastProgressEntity?

    suspend fun getAllProgressForPodcast(podcastId: Int): List<PodcastProgressEntity>

    suspend fun getRecentProgress(): List<PodcastProgressEntity>


    fun getHistoryFlow(): Flow<List<EpisodeHistoryEntity>>

    suspend fun insertEpisodeHistory(history: EpisodeHistoryEntity)
}