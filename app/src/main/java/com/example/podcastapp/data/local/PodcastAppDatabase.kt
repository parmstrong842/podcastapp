package com.example.podcastapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.podcastapp.data.local.entities.SubscribedPodcast

@Database(entities = [SubscribedPodcast::class], version = 3, exportSchema = false)
abstract class PodcastAppDatabase : RoomDatabase() {

    abstract fun databaseDao(): DatabaseDao

    companion object {
        @Volatile
        private var Instance: PodcastAppDatabase? = null

        fun getDatabase(context: Context): PodcastAppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PodcastAppDatabase::class.java, "podcast_app_database")
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}