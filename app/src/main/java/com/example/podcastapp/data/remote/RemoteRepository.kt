package com.example.podcastapp.data.remote

import com.example.podcastapp.data.remote.models.podcastindex.EpisodeFeedResponse
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeResponse
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse

interface RemoteRepository {

    suspend fun searchPodcastsByTerm(query: String): SearchResponse

    suspend fun episodesByFeedID(id: Int): EpisodeFeedResponse

    suspend fun episodeByID(id: Long): EpisodeResponse
}