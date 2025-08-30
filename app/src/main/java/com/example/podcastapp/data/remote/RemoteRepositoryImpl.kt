package com.example.podcastapp.data.remote

import com.example.podcastapp.AuthManager.generateAuthHeaders
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeFeedResponse
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeResponse
import com.example.podcastapp.data.remote.models.podcastindex.PodcastByFeedIDResponse
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse
import okhttp3.ResponseBody

class RemoteRepositoryImpl(
    private val podcastIndexApi: PodcastIndexApi,
    private val rssService: RssService
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

    override suspend fun podcastByFeedID(id: Int): PodcastByFeedIDResponse {
        val headers = generateAuthHeaders()
        return podcastIndexApi.podcastByFeedID(
            authDate = headers["X-Auth-Date"]!!,
            authorization = headers["Authorization"]!!,
            id = id
        )
    }

    override suspend fun episodesByFeedID(id: Int): EpisodeFeedResponse {
        val headers = generateAuthHeaders()
        return podcastIndexApi.episodesByFeedID(
            authDate = headers["X-Auth-Date"]!!,
            authorization = headers["Authorization"]!!,
            id = id
        )
    }

    override suspend fun episodeByID(id: Long): EpisodeResponse {
        val headers = generateAuthHeaders()
        return podcastIndexApi.episodeByID(
            authDate = headers["X-Auth-Date"]!!,
            authorization = headers["Authorization"]!!,
            id = id
        )
    }

    override suspend fun getRssFeedContent(feedUrl: String): ResponseBody {
        return rssService.getFeed(feedUrl)
    }
}