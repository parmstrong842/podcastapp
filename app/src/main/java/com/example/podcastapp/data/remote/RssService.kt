package com.example.podcastapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Url

interface RssService {
    @GET
    suspend fun getFeed(@Url url: String): okhttp3.ResponseBody
}