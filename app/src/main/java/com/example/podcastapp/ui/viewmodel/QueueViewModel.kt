package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.ui.components.PodcastEpItem
import com.example.podcastapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class QueueFetchState(
    val queue: List<PodcastEpItem>
)

data class QueueUiState(
    val queueFetchState: Resource<QueueFetchState>
)

class QueueViewModel(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<QueueUiState> = MutableStateFlow(QueueUiState(
        queueFetchState = Resource.Loading
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
//            databaseRepository
//                .getQueueFlow()
//                .catch {
//                    _uiState.update { it.copy(queueFetchState = Resource.Error) }
//                }
//                .collect { list ->
//                    _uiState.update { state ->
//                        state.copy(
//                            queueFetchState = Resource.Success(
//                                QueueFetchState(queue = list.map { it.toPodcastEpItem() })
//                            )
//                        )
//                    }
//                }
        }
    }

    class Factory(
        private val application: PodcastApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val databaseRepository = application.container.databaseRepository
            return QueueViewModel(databaseRepository) as T
        }
    }
}