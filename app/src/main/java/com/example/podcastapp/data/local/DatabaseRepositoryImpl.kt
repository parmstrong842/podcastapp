package com.example.podcastapp.data.local

import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import kotlinx.coroutines.flow.Flow

class DatabaseRepositoryImpl(private val dao: DatabaseDao) : DatabaseRepository {

    override fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcastEntity>> {
        return dao.getAllSubscriptionsFlow()
    }

    override suspend fun getSubscription(id: Int): SubscribedPodcastEntity {
        return dao.getSubscription(id)
    }

    override suspend fun insertSubscription(item: SubscribedPodcastEntity) {
        dao.insertSubscription(item)
    }

    override suspend fun updateSubscription(item: SubscribedPodcastEntity) {
        dao.updateSubscription(item)
    }

    override suspend fun deleteSubscription(item: SubscribedPodcastEntity) {
        dao.deleteSubscription(item)
    }


    override suspend fun saveProgress(progress: PodcastProgressEntity) {
        dao.saveProgress(progress)
    }

    override suspend fun getProgress(podcastId: Int, episodeId: Long): PodcastProgressEntity? {
        return dao.getProgress(podcastId, episodeId)
    }

    override suspend fun getRecentProgress(): List<PodcastProgressEntity> {
        return dao.getRecentProgress()
    }
}