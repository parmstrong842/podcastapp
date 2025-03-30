package com.example.podcastapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.podcastapp.data.local.entities.PodcastProgressEntity
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

    @Query("SELECT * from subscriptions")
    fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcastEntity>>

    @Query("SELECT * from subscriptions WHERE id = :id")
    suspend fun getSubscription(id: Int): SubscribedPodcastEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubscription(item: SubscribedPodcastEntity)

    @Update
    suspend fun updateSubscription(item: SubscribedPodcastEntity)

    @Delete
    suspend fun deleteSubscription(item: SubscribedPodcastEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: PodcastProgressEntity)

    @Query("SELECT * FROM podcast_progress WHERE podcastId = :podcastId AND episodeId = :episodeId")
    suspend fun getProgress(podcastId: Int, episodeId: Long): PodcastProgressEntity?

    @Query("SELECT * FROM podcast_progress ORDER BY timestamp DESC")
    suspend fun getRecentProgress(): List<PodcastProgressEntity>
}