package com.example.podcastapp.data.remote

import com.example.podcastapp.data.remote.models.podcastindex.EpisodeResponse
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse

interface RemoteRepository {

    suspend fun searchPodcastsByTerm(query: String): SearchResponse

    suspend fun episodeByFeedID(id: Int): EpisodeResponse
}