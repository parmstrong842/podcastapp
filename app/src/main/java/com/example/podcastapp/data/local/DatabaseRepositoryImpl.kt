package com.example.podcastapp.data.local

import com.example.podcastapp.data.local.entities.SubscribedPodcast
import kotlinx.coroutines.flow.Flow

class DatabaseRepositoryImpl(private val databaseDao: DatabaseDao) : DatabaseRepository {

    override fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcast>> {
        return databaseDao.getAllSubscriptionsFlow()
    }

    override suspend fun getSubscription(id: Int): SubscribedPodcast {
        return databaseDao.getSubscription(id)
    }

    override suspend fun insertSubscription(item: SubscribedPodcast) {
        databaseDao.insertSubscription(item)
    }

    override suspend fun updateSubscription(item: SubscribedPodcast) {
        databaseDao.updateSubscription(item)
    }

    override suspend fun deleteSubscription(item: SubscribedPodcast) {
        databaseDao.deleteSubscription(item)
    }
}