package com.example.podcastapp.data.local

import androidx.room.withTransaction
import com.example.podcastapp.data.local.entities.EpisodeHistoryEntity
import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.example.podcastapp.data.local.entities.QueueEntity
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import com.example.podcastapp.ui.components.PodcastEpItem
import com.example.podcastapp.utils.toQueueEntity
import kotlinx.coroutines.flow.Flow

class DatabaseRepositoryImpl(
    private val dao: DatabaseDao,
    private val db: PodcastAppDatabase
) : DatabaseRepository {

    override fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcastEntity>> {
        return dao.getAllSubscriptionsFlow()
    }

    override suspend fun getSubscription(feedUrl: String): SubscribedPodcastEntity? {
        return dao.getSubscription(feedUrl)
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

    override suspend fun getProgress(feedUrl: String, guid: Long): PodcastProgressEntity? {
        return dao.getProgress(feedUrl, guid)
    }

    override suspend fun getAllProgressForPodcast(feedUrl: String): List<PodcastProgressEntity> {
            return dao.getAllProgressForPodcast(feedUrl)
    }

    override suspend fun getRecentProgress(): List<PodcastProgressEntity> {
        return dao.getRecentProgress()
    }



    override fun getHistoryFlow(): Flow<List<EpisodeHistoryEntity>> {
        return dao.getHistoryFlow()
    }

    override suspend fun insertEpisodeHistory(history: EpisodeHistoryEntity) {
        dao.insertEpisodeHistory(history)
    }



    override fun getQueueFlow(): Flow<List<QueueEntity>> {
        return dao.getQueueFlow()
    }

    override suspend fun enqueue(podcastEpItem: PodcastEpItem) {
        val next = dao.maxPosition() + 1
        dao.insert(
            podcastEpItem.toQueueEntity(next)
        )
    }

    override suspend fun remove(key: String) = db.withTransaction {
        val pos = dao.positionOf(key) ?: return@withTransaction
        dao.deleteByKey(key)
        dao.collapseFrom(pos)
    }

    override suspend fun move(key: String, newIndex: Int) = db.withTransaction {
        val list = dao.getQueueOnce()
        val oldIndex = list.indexOfFirst { it.key == key }
        if (oldIndex == -1 || newIndex == oldIndex) return@withTransaction

        if (oldIndex < newIndex) {
            // moving down: items (fromIndex+1..newIndex) shift up (-1)
            val start = list[oldIndex + 1].queuePosition
            val end = list[newIndex].queuePosition
            dao.decRange(start, end)
        } else {
            // moving up: items (newIndex..fromIndex-1) shift down (+1)
            val start = list[newIndex].queuePosition
            val end = list[oldIndex - 1].queuePosition
            dao.incRange(start, end)
        }
        dao.setPosition(key, newIndex)
    }
}