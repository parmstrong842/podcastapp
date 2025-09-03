package com.example.podcastapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.podcastapp.data.local.entity.EpisodeEntity
import com.example.podcastapp.data.local.entity.EpisodeStateEntity
import com.example.podcastapp.data.local.entity.PodcastEntity
import com.example.podcastapp.data.local.model.EpisodeProgress
import com.example.podcastapp.data.local.model.EpisodeWithState
import com.example.podcastapp.ui.components.PodcastEpItem
import kotlinx.coroutines.flow.Flow

/*
subscribe/unsubscribe to podcast
get list of subscribed podcasts

get progress of podcast episodes
get episode history

get episode queue
 */

@Dao
interface DatabaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPodcast(podcast: PodcastEntity): Long

    @Upsert
    suspend fun upsertPodcast(podcast: PodcastEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Query("UPDATE podcasts SET subscribed = 0 WHERE feedUrl = :feedUrl")
    suspend fun unsubscribe(feedUrl: String)

    @Query("SELECT * FROM podcasts WHERE subscribed = 1")
    fun getAllSubscriptionsFlow(): Flow<List<PodcastEntity>>

    @Query("SELECT subscribed FROM podcasts WHERE feedUrl = :feedUrl")
    suspend fun isSubscribed(feedUrl: String): Boolean?

    @Query(
        """
        SELECT 
            e.episodeImage,
            e.pubDate,
            e.episodeTitle,
            e.episodeDescription,
            e.enclosureUrl,
            e.feedUrl,
            e.guid,
            s.duration,
            s.position,
            s.finished,
            p.podcastTitle,
            p.podcastImage
        FROM episodes e
        JOIN episode_state s ON s.episodeId = e.episodeId
        JOIN podcasts p ON p.feedUrl = e.feedUrl
        WHERE s.lastPlayedAt IS NOT NULL
        ORDER BY s.lastPlayedAt DESC
    """
    )
    fun getHistoryFlow(): Flow<List<EpisodeWithState>>

    @Query("SELECT episodeId FROM episodes WHERE feedUrl = :feedUrl AND guid = :guid")
    suspend fun findEpisodeId(feedUrl: String, guid: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ensureStateRow(state: EpisodeStateEntity): Long

    @Query("""
        UPDATE episode_state
        SET lastPlayedAt = :now
        WHERE episodeId = :episodeId
    """)
    suspend fun updateLastPlayedAt(episodeId: Long, now: Long)

    @Transaction
    suspend fun insertEpisodeHistory(pod: PodcastEpItem, now: Long = System.currentTimeMillis()) {
        val episodeId = ensureEverythingExists(pod) ?: return
        updateLastPlayedAt(episodeId, now)
    }

    @Query("""
        UPDATE episode_state
        SET position = :position,
            duration = :duration,
            finished = :finished
        WHERE episodeId = :episodeId
    """)
    suspend fun updateProgress(episodeId: Long, position: Long, duration: Long, finished: Boolean)

    @Transaction
    suspend fun saveProgress(feedUrl: String, guid: String, position: Long, duration: Long, finished: Boolean) {
        val episodeId = findEpisodeId(feedUrl, guid) ?: return
        updateProgress(episodeId, position, duration, finished)
    }

    @Query("""
        SELECT s.position, s.duration, s.finished
        FROM episode_state s
        JOIN episodes e ON e.episodeId = s.episodeId
        WHERE e.feedUrl = :feedUrl AND e.guid = :guid
    """)
    suspend fun getProgress(feedUrl: String, guid: String): EpisodeProgress?

    @Transaction
    suspend fun ensureEverythingExists(pod: PodcastEpItem): Long? {
        insertPodcast(PodcastEntity(
            feedUrl = pod.feedUrl,
            podcastTitle = pod.podcastTitle,
            podcastImage = pod.podcastImage
        ))
        insertEpisode(EpisodeEntity(
            feedUrl = pod.feedUrl,
            guid = pod.guid,
            episodeTitle = pod.episodeTitle,
            episodeDescription = pod.episodeDescription,
            enclosureUrl = pod.enclosureUrl,
            episodeImage = pod.episodeImage,
            pubDate = pod.pubDate
        ))
        val episodeId = findEpisodeId(pod.feedUrl, pod.guid) ?: return null
        ensureStateRow(EpisodeStateEntity(episodeId))
        return episodeId
    }
}