package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.entity.PodcastEntity
import com.example.podcastapp.data.remote.RemoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val subscriptions: List<PodcastEntity>
)

class HomeViewModel(
    private val remoteRepository: RemoteRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = databaseRepository.getAllSubscriptionsFlow()
        .map { subscriptions ->
            HomeUiState(subscriptions = subscriptions)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(subscriptions = emptyList())
        )

    fun subscribeToPodcast(rssUrl: String) {
        viewModelScope.launch {
            val channel = remoteRepository.getRssChannel(rssUrl)

            val podcastTitle = channel.title.orEmpty()
            val podcastImage = channel.itunesChannelData?.image
                ?: channel.image?.url.orEmpty()

            databaseRepository.subscribe(
                feedUrl = rssUrl,
                podcastTitle = podcastTitle,
                image = podcastImage.takeIf { it.isNotEmpty() }
            )
        }
    }

    class Factory(
        private val application: PodcastApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val remoteRepository = application.container.remoteRepository
            val databaseRepository = application.container.databaseRepository
            return HomeViewModel(remoteRepository, databaseRepository) as T
        }
    }
}
