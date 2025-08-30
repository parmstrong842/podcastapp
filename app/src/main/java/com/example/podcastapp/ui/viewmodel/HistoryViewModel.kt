package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.ui.components.PodcastEpItem
import com.example.podcastapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class HistoryFetchState(
    val history: List<PodcastEpItem>
)

data class HistoryUiState(
    val historyFetchState: Resource<HistoryFetchState>
)

class HistoryViewModel(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<HistoryUiState> = MutableStateFlow(HistoryUiState(
        historyFetchState = Resource.Loading
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            databaseRepository
                .getHistoryFlow()
                .catch {
                    _uiState.update { it.copy(historyFetchState = Resource.Error) }
                }
                .collect { list ->
                    _uiState.update { state ->
                        state.copy(
                            historyFetchState = Resource.Success(
                                HistoryFetchState(history = list)
                            )
                        )
                    }
                }
        }
    }
//
//    fun enqueue(item: PodcastEpItem) {
//        viewModelScope.launch {
//            databaseRepository.enqueue(item)
//        }
//    }
//
//    fun removeFromQueue(item: PodcastEpItem) {
//        viewModelScope.launch {
//            databaseRepository.remove("${item.feedUrl}#${item.guid}")
//        }
//    }

    class Factory(
        private val application: PodcastApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val databaseRepository = application.container.databaseRepository
            return HistoryViewModel(databaseRepository) as T
        }
    }
}