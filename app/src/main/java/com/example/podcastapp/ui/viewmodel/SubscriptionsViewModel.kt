package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.data.local.DatabaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SubscriptionsViewModel(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {
    val uiState: StateFlow<UserUiState> = databaseRepository.getAllSubscriptionsFlow()
        .map { subscriptions ->
            UserUiState(subscriptions = subscriptions)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserUiState(subscriptions = emptyList())
        )
}