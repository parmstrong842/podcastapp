package com.example.podcastapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.podcastapp.data.local.entities.SubscribedPodcast
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

    @Query("SELECT * from subscriptions")
    fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcast>>

    @Query("SELECT * from subscriptions WHERE id = :id")
    suspend fun getSubscription(id: Int): SubscribedPodcast

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubscription(item: SubscribedPodcast)

    @Update
    suspend fun updateSubscription(item: SubscribedPodcast)

    @Delete
    suspend fun deleteSubscription(item: SubscribedPodcast)
}