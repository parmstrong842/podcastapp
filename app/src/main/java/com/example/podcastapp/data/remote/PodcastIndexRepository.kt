package com.example.podcastapp.data.remote

import com.example.podcastapp.AuthManager.generateAuthHeaders
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeResponse
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse

class PodcastIndexRepository(
    private val podcastIndexApi: PodcastIndexApi
) : RemoteRepository {

    override suspend fun searchPodcastsByTerm(
        query: String
    ): SearchResponse {
        val headers = generateAuthHeaders()
        return podcastIndexApi.searchPodcastsByTerm(
            authDate = headers["X-Auth-Date"]!!,
            authorization = headers["Authorization"]!!,
            query = query
        )
    }

    override suspend fun episodeByFeedID(id: Int): EpisodeResponse {
        val headers = generateAuthHeaders()
        return podcastIndexApi.episodeByFeedID(
            authDate = headers["X-Auth-Date"]!!,
            authorization = headers["Authorization"]!!,
            id = id
        )
    }
}