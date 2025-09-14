package com.example.podcastapp.data.remote

import com.example.podcastapp.data.remote.AuthManager.generateAuthHeaders
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeFeedResponse
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeResponse
import com.example.podcastapp.data.remote.models.podcastindex.PodcastByFeedIDResponse
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import okhttp3.OkHttpClient

class RemoteRepositoryImpl(
    private val podcastIndexApi: PodcastIndexApi,
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

    override suspend fun getRssChannel(feedUrl: String): RssChannel {
        val client = OkHttpClient.Builder()
            .addInterceptor { ch ->
                val req = ch.request().newBuilder()
                    .header("User-Agent", "MyPodcastApp/1.0 (+https://example.com)")
                    .header("Accept", "application/rss+xml, application/xml;q=0.9, */*;q=0.8")
                    .build()
                ch.proceed(req)
            }
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        val parser = RssParserBuilder(callFactory = client).build()
        return parser.getRssChannel(feedUrl)
    }
}