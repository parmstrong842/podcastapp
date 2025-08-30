package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.entity.PodcastEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val subscriptions: List<PodcastEntity>
)

class HomeViewModel(
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

    class Factory(
        private val application: PodcastApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val databaseRepository = application.container.databaseRepository
            return HomeViewModel(databaseRepository) as T
        }
    }
}
