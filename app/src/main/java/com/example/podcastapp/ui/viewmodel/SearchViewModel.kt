package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import com.example.podcastapp.data.remote.RemoteRepository
import com.example.podcastapp.data.remote.models.podcastindex.Podcast
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val tag = "searchTag"

sealed interface SearchResult {
    data class Success(val podcasts: List<Podcast>) : SearchResult
    data object Error : SearchResult
    data object Loading : SearchResult
    data object Idle : SearchResult
}

data class SearchUiState(
    val query: String,
    val searchResult: SearchResult
)

class SearchViewModel(
    private val remoteRepository: RemoteRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState(
        query = "",
        searchResult = SearchResult.Idle
    ))
    val uiState = _uiState.asStateFlow()

    init {
        observeQueryChanges()
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryChanges() {
        viewModelScope.launch {
            _uiState
                .map { it.query }
                .distinctUntilChanged()
                .debounce(300)
                .collect {
                    if (_uiState.value.query.isNotBlank()) {
                        performSearch(_uiState.value.query)
                    } else {
                        _uiState.update { it.copy(searchResult = SearchResult.Idle) }
                    }
                }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(searchResult = SearchResult.Loading) }

        val searchResult = try {
            val response = remoteRepository.searchPodcastsByTerm(
                query
            )
            SearchResult.Success(response.feeds)
        } catch (e: Exception) {
            SearchResult.Error
        }
        _uiState.update {
            it.copy(
                searchResult = searchResult
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                query = query
            )
        }
    }

    fun subscribeToPodcast(podcast: Podcast) {
        viewModelScope.launch {
            databaseRepository.insertSubscription(
                SubscribedPodcastEntity(
                    id = podcast.id,
                    title = podcast.title,
                    image = podcast.image
                )
            )
        }
    }
}
