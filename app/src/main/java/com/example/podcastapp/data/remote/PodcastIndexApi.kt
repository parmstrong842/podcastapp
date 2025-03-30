package com.example.podcastapp.data.remote

import com.example.podcastapp.data.remote.models.podcastindex.EpisodeFeedResponse
import com.example.podcastapp.data.remote.models.podcastindex.EpisodeResponse
import com.example.podcastapp.data.remote.models.podcastindex.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PodcastIndexApi {

    @GET("search/byterm")
    suspend fun searchPodcastsByTerm(
        @Header("X-Auth-Date") authDate: String,
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
    ): SearchResponse

    @GET("episodes/byfeedid")
    suspend fun episodesByFeedID(
        @Header("X-Auth-Date") authDate: String,
        @Header("Authorization") authorization: String,
        @Query("id") id: Int,
    ): EpisodeFeedResponse

    @GET("episodes/byid")
    suspend fun episodeByID(
        @Header("X-Auth-Date") authDate: String,
        @Header("Authorization") authorization: String,
        @Query("id") id: Long,
    ): EpisodeResponse
}