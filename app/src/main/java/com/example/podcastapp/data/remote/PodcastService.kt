package com.example.podcastapp.data.remote

import com.example.podcastapp.data.remote.models.podcastindex.Episode
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PodcastService {
    @GET("search/byterm")
    suspend fun searchPodcasts(
        @Header("X-Auth-Date") authDate: String,
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("max") maxResults: Int = 20
    ): SearchResponse

    @GET("episodes/byfeedid")
    suspend fun getPodcastEpisodes(
        @Header("X-Auth-Date") authDate: String,
        @Header("Authorization") authorization: String,
        @Query("id") podcastId: Int
    ): PodcastEpisodesResponse
}

data class PodcastEpisodesResponse(
    val count: Int,
    val items: List<Episode>
)