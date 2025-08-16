package com.example.podcastapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import com.example.podcastapp.data.local.newentities.EpisodeEntity
import com.example.podcastapp.data.local.newentities.EpisodeStateEntity
import kotlinx.coroutines.flow.Flow

/*
subscribe/unsubscribe to podcast
get list of subscribed podcasts

get progress of podcast episodes

get episode history

get episode queue
 */

@Dao
interface NewDatabaseDao {
    @Query("SELECT * from subscriptions")
    fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcastEntity>>

    @Query("SELECT subscribed FROM podcasts WHERE feedUrl = :feedUrl")
    suspend fun isSubscribed(feedUrl: String): Boolean?

    @Query("UPDATE podcasts SET subscribed = 1 WHERE feedUrl = :feedUrl")
    suspend fun subscribe(feedUrl: String)

    @Query("UPDATE podcasts SET subscribed = 0 WHERE feedUrl = :feedUrl")
    suspend fun unsubscribe(feedUrl: String)



//    @Upsert
//    suspend fun upsertEpisode(episode: EpisodeEntity): Long
//
//    @Query("SELECT episodeId FROM episodes WHERE feedUrl = :feedUrl AND guid = :guid")
//    suspend fun episodeIdFor(feedUrl: String, guid: Long): Long?
//
//
//
//    @Upsert
//    suspend fun upsertState(state: EpisodeStateEntity)
//
//    @Query("SELECT * FROM episode_state WHERE episodeId = :episodeId")
//    suspend fun getState(episodeId: Long): EpisodeStateEntity?
//
//    @Query("UPDATE episode_state SET position = :position, duration = :duration, finished = :finished, updatedAt = :updatedAt WHERE episodeId = :episodeId")
//    suspend fun updateProgress(episodeId: Long, position: Long, duration: Long, finished: Boolean, updatedAt: Long = System.currentTimeMillis())
//
//
//
//    @Transaction
//    suspend fun addToHistory(episodeId: Long, ts: Long = System.currentTimeMillis()) {
//        val current = getState(episodeId)
//        if (current == null) {
//            upsertState(EpisodeStateEntity(episodeId = episodeId, lastPlayedAt = ts))
//        } else if (current.lastPlayedAt != ts) {
//            upsertState(current.copy(lastPlayedAt = ts))
//        }
//    }
//
//    @Query("""
//        SELECT e.* FROM episodes e
//        JOIN episode_state s ON s.episodeId = e.episodeId
//        WHERE s.lastPlayedAt IS NOT NULL
//        ORDER BY s.lastPlayedAt DESC
//    """)
//    fun historyFlow(): kotlinx.coroutines.flow.Flow<List<EpisodeEntity>>
}