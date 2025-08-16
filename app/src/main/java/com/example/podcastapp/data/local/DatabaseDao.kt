package com.example.podcastapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.podcastapp.data.local.entities.EpisodeHistoryEntity
import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.example.podcastapp.data.local.entities.QueueEntity
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

    // Subscribed Podcasts
    @Query("SELECT * from subscriptions")
    fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcastEntity>>

    @Query("SELECT * from subscriptions WHERE feedUrl = :feedUrl")
    suspend fun getSubscription(feedUrl: String): SubscribedPodcastEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubscription(item: SubscribedPodcastEntity)

    @Update
    suspend fun updateSubscription(item: SubscribedPodcastEntity)

    @Delete
    suspend fun deleteSubscription(item: SubscribedPodcastEntity)


    // Podcast Progress
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: PodcastProgressEntity)

    @Query("SELECT * FROM podcast_progress WHERE feedUrl = :feedUrl AND guid = :guid")
    suspend fun getProgress(feedUrl: String, guid: Long): PodcastProgressEntity?

    @Query("SELECT * FROM podcast_progress WHERE feedUrl = :feedUrl")
    suspend fun getAllProgressForPodcast(feedUrl: String): List<PodcastProgressEntity>

    @Query("SELECT * FROM podcast_progress ORDER BY timestamp DESC")
    suspend fun getRecentProgress(): List<PodcastProgressEntity>


    // Episode History
    @Query("SELECT * from episode_history ORDER BY playedAtMillis DESC")
    fun getHistoryFlow(): Flow<List<EpisodeHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodeHistory(history: EpisodeHistoryEntity)


    // Queue Management
    @Query("SELECT * FROM episode_queue ORDER BY queuePosition ASC")
    fun getQueueFlow(): Flow<List<QueueEntity>>

    @Query("SELECT * FROM episode_queue ORDER BY queuePosition ASC")
    suspend fun getQueueOnce(): List<QueueEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: QueueEntity)

    @Query("DELETE FROM episode_queue WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT queuePosition FROM episode_queue WHERE `key` = :key LIMIT 1")
    suspend fun positionOf(key: String): Int?

    @Query("UPDATE episode_queue SET queuePosition = queuePosition - 1 WHERE queuePosition > :pos")
    suspend fun collapseFrom(pos: Int)

    @Query("UPDATE episode_queue SET queuePosition = :position WHERE `key` = :key")
    suspend fun setPosition(key: String, position: Int)

    @Query("UPDATE episode_queue SET queuePosition = queuePosition + 1 WHERE queuePosition BETWEEN :start AND :end")
    suspend fun incRange(start: Int, end: Int)

    @Query("UPDATE episode_queue SET queuePosition = queuePosition - 1 WHERE queuePosition BETWEEN :start AND :end")
    suspend fun decRange(start: Int, end: Int)

    @Query("SELECT COALESCE(MAX(queuePosition), -1) FROM episode_queue")
    suspend fun maxPosition(): Int
}