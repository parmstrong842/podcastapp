package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.data.remote.RemoteRepository
import com.example.podcastapp.ui.components.PodcastEpItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExploreUIState(
    val list: List<PodcastSquare>,
    val items: List<PodcastEpItem>
)

data class PodcastSquare(
    val imageResID: Int,
    val title: String,
    val publisher: String
)

class ExploreViewModel(
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<ExploreUIState>
    val uiState: StateFlow<ExploreUIState>

    init {

        _uiState = MutableStateFlow(
            ExploreUIState(
                emptyList(),
                emptyList()
            )
        )
        uiState = _uiState.asStateFlow()
    }

    class Factory(
        private val application: PodcastApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val remoteRepository = application.container.remoteRepository
            return ExploreViewModel(remoteRepository) as T
        }
    }
}
