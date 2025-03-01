package com.example.podcastapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.remote.RemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException


data class PodcastUiState(
    val title: String,
    val image: String?,
    val episodes: List<PodcastEpItem>
)

class PodcastViewModel(
    savedStateHandle: SavedStateHandle,
    private val databaseRepository: DatabaseRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val podcastId: Int = checkNotNull(savedStateHandle["podcastId"])

    private val _uiState: MutableStateFlow<PodcastUiState> = MutableStateFlow(PodcastUiState(
        title = "",
        image = "",
        episodes = emptyList()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val subscribedPodcast = databaseRepository.getSubscription(podcastId)
            _uiState.update {
                it.copy(
                    title = subscribedPodcast.title,
                    image = subscribedPodcast.image
                )
            }
            try {
                val response = remoteRepository.episodeByFeedID(subscribedPodcast.id)
                val episodes = response.items.map {
                    PodcastEpItem(
                        image = it.image,
                        title = subscribedPodcast.title,
                        uploadDate = it.datePublishedPretty,
                        episodeName = it.title,
                        description = it.description,
                        enclosureUrl = it.enclosureUrl,
                        timeLeft = it.duration.toString() // TODO: make pretty
                    )
                }
                _uiState.update {
                    it.copy(
                        episodes = episodes
                    )
                }
            } catch (e: HttpException) { // TODO: handle exceptions
                throw e
            }
        }
    }
}