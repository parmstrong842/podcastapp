package com.example.podcastapp.data.remote

import com.example.podcastapp.data.remote.models.podcastindex.EpisodeFeedResponse
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeResponse
import com.example.podcastapp.data.remote.models.podcastindex.PodcastByFeedIDResponse
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse
import com.prof18.rssparser.model.RssChannel

interface RemoteRepository {

    suspend fun searchPodcastsByTerm(query: String): SearchResponse

    suspend fun podcastByFeedID(id: Int): PodcastByFeedIDResponse

    suspend fun episodesByFeedID(id: Int): EpisodeFeedResponse

    suspend fun episodeByID(id: Long): EpisodeResponse

    suspend fun getRssChannel(feedUrl: String): RssChannel
}