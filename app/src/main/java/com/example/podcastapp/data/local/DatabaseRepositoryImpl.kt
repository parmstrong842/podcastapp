package com.example.podcastapp.data.local

import com.example.podcastapp.audiocontroller.EpisodeMetadata
import com.example.podcastapp.data.local.entity.PodcastEntity
import com.example.podcastapp.data.local.mapper.toPodcastEpItem
import com.example.podcastapp.data.local.model.EpisodeProgress
import com.example.podcastapp.ui.components.PodcastEpItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatabaseRepositoryImpl(
    private val dao: DatabaseDao,
    private val db: PodcastAppDatabase
) : DatabaseRepository {

    override suspend fun subscribe(feedUrl: String, podcastTitle: String, image: String?) {
        dao.upsertPodcast(PodcastEntity(feedUrl = feedUrl, podcastTitle = podcastTitle, podcastImage = image, subscribed = true))
    }

    override suspend fun unsubscribe(feedUrl: String) {
        dao.unsubscribe(feedUrl)
    }

    override fun getAllSubscriptionsFlow(): Flow<List<PodcastEntity>> {
        return dao.getAllSubscriptionsFlow()
    }

    override suspend fun isSubscribed(feedUrl: String): Boolean? {
        return dao.isSubscribed(feedUrl)
    }

    override suspend fun saveProgress(feedUrl: String, guid: String, position: Long, duration: Long, finished: Boolean) {
        dao.saveProgress(feedUrl, guid, position, duration, finished)
    }

    override suspend fun getProgress(feedUrl: String, guid: String): EpisodeProgress? {
        return dao.getProgress(feedUrl, guid)
    }

    override suspend fun getAllProgressForPodcast(feedUrl: String): List<PodcastEpItem> {
        return dao.getAllProgressForPodcast(feedUrl).map { it.toPodcastEpItem() }
    }


    override suspend fun insertEpisodeHistory(metadata: EpisodeMetadata, duration: Long) {
        dao.insertEpisodeHistory(metadata, duration)
    }

    override fun getHistoryFlow(): Flow<List<PodcastEpItem>> {
        return dao.getHistoryFlow().map { list ->
            list.map { it.toPodcastEpItem() }
        }
    }

}