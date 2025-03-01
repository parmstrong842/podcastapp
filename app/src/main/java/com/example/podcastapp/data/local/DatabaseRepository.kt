package com.example.podcastapp.data.local

import com.example.podcastapp.data.local.entities.SubscribedPodcast
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {

    fun getAllSubscriptionsFlow(): Flow<List<SubscribedPodcast>>

    suspend fun getSubscription(id: Int): SubscribedPodcast

    suspend fun insertSubscription(item: SubscribedPodcast)

    suspend fun updateSubscription(item: SubscribedPodcast)

    suspend fun deleteSubscription(item: SubscribedPodcast)
}