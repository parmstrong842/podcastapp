package com.example.podcastapp.data

import android.content.Context
import com.example.podcastapp.BuildConfig
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.DatabaseRepositoryImpl
import com.example.podcastapp.data.local.PodcastAppDatabase
import com.example.podcastapp.data.remote.PodcastIndexApi
import com.example.podcastapp.data.remote.PodcastIndexRepository
import com.example.podcastapp.data.remote.RemoteRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppDataContainer(private val context: Context) {

    private val PODCAST_INDEX_URL = "https://api.podcastindex.org/api/1.0/"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
            request.addHeader("X-Auth-Key", BuildConfig.PODCAST_API_KEY)
            request.addHeader("User-Agent", "GoodPodcastApp/1.0")
            chain.proceed(request.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val podcastIndexApi: PodcastIndexApi by lazy {
        Retrofit.Builder()
            .baseUrl(PODCAST_INDEX_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PodcastIndexApi::class.java)
    }

    val remoteRepository: RemoteRepository by lazy {
        PodcastIndexRepository(podcastIndexApi)
    }

    val databaseRepository: DatabaseRepository by lazy {
        val db = PodcastAppDatabase.getDatabase(context)
        DatabaseRepositoryImpl(db.databaseDao(), db)
    }
}