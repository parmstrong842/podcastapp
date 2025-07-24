package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.utils.Resource
import com.example.podcastapp.utils.toPodcastEpItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
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
                .catch { throwable ->
                    _uiState.update { it.copy(historyFetchState = Resource.Error) }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            historyFetchState = Resource.Success(
                                HistoryFetchState(history = list.map { it.toPodcastEpItem() })
                            )
                        )
                    }
                }
        }
    }
}