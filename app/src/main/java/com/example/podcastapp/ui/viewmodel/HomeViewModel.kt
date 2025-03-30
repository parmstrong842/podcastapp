package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val subscriptions: List<SubscribedPodcastEntity>
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
}
