package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.ui.screens.EpisodeScreen

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                podcastApplication().container.databaseRepository
            )
        }
        initializer {
            ExploreViewModel(podcastApplication().container.remoteRepository)
        }
        initializer {
            UserViewModel()
        }
        initializer {
            QueueViewModel()
        }
        initializer {
            DownloadsViewModel()
        }
        initializer {
            HistoryViewModel(
                podcastApplication().container.databaseRepository
            )
        }
        initializer {
            SubscriptionsViewModel(podcastApplication().container.databaseRepository)
        }
        initializer {
            PodcastViewModel(
                this.createSavedStateHandle(),
                podcastApplication().container.databaseRepository,
                podcastApplication().container.remoteRepository
            )
        }
        initializer {
            EpisodeViewModel(
                this.createSavedStateHandle(),
                podcastApplication().container.databaseRepository,
                podcastApplication().container.remoteRepository
            )
        }
        initializer {
            SearchViewModel(
                podcastApplication().container.remoteRepository,
                podcastApplication().container.databaseRepository
            )
        }
    }
}

fun CreationExtras.podcastApplication(): PodcastApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PodcastApplication)