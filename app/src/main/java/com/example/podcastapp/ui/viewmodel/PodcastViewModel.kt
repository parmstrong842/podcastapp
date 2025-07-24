package com.example.podcastapp.ui.viewmodel

import android.R.attr.data
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import com.example.podcastapp.data.local.entities.progressFraction
import com.example.podcastapp.data.local.entities.timeLeftMs
import com.example.podcastapp.data.remote.RemoteRepository
import com.example.podcastapp.utils.Resource
import com.example.podcastapp.utils.formatTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


private const val tag = "PodcastViewModel"

data class PodcastFetchState(
    val title: String,
    val image: String,
    val subscribed: Boolean,
    val episodes: List<PodcastEpItem>
)

data class PodcastUiState(
    val sortByTabSelection: String,
    val podcastFetchState: Resource<PodcastFetchState>
)

class PodcastViewModel(
    savedStateHandle: SavedStateHandle,
    private val databaseRepository: DatabaseRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val podcastId: Int = checkNotNull(savedStateHandle["podcastId"])

    private val _uiState: MutableStateFlow<PodcastUiState> = MutableStateFlow(PodcastUiState(
        sortByTabSelection = "Latest",
        podcastFetchState = Resource.Loading
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val podcastFetchResult = try {
                val podcastResponse = remoteRepository.podcastByFeedID(podcastId)
                val response = remoteRepository.episodesByFeedID(podcastId)
                val allProgress = databaseRepository.getAllProgressForPodcast(podcastId)
                val episodes = response.items.map {
                    val progress = allProgress.find { p -> p.episodeId == it.id }
                    val remainingMs = progress?.timeLeftMs() ?: (it.duration.toLong() * 1000L)
                    val timeLeft = formatTime(remainingMs)
                    PodcastEpItem(
                        image = it.image,
                        title = podcastResponse.feed.title,
                        datePublishedPretty = it.datePublishedPretty,
                        datePublished = it.datePublished,
                        episodeName = it.title,
                        description = it.description,
                        enclosureUrl = it.enclosureUrl,
                        timeLeft = timeLeft,
                        progress = progress?.progressFraction() ?: 0f,
                        podcastId = podcastId,
                        episodeId = it.id,
                        played = progress?.finished == true
                    )
                }
                Resource.Success(
                    PodcastFetchState(
                        title = podcastResponse.feed.title,
                        image = podcastResponse.feed.image,
                        subscribed = databaseRepository.getSubscription(podcastId) != null,
                        episodes = episodes

                    )
                )
            } catch (e: Exception) {
                Resource.Error
            }
            _uiState.update {
                it.copy(
                    podcastFetchState = podcastFetchResult
                )
            }
        }
    }

    fun subscribeToPodcast() {
        viewModelScope.launch {
            val current = _uiState.value.podcastFetchState
            if (current is Resource.Success) {
                val data = current.data
                runCatching {
                    databaseRepository.insertSubscription(
                        SubscribedPodcastEntity(
                            id    = podcastId,
                            title = data.title,
                            image = data.image
                        )
                    )
                }.onSuccess {
                    val updated = data.copy(subscribed = true)
                    _uiState.update {
                        it.copy(podcastFetchState = Resource.Success(updated))
                    }
                }
            }
        }
    }

    fun unsubscribeToPodcast() {
        viewModelScope.launch {
            val current = _uiState.value.podcastFetchState
            if (current is Resource.Success) {
                val data = current.data
                runCatching {
                    databaseRepository.deleteSubscription(
                        SubscribedPodcastEntity(
                            id    = podcastId,
                            title = data.title,
                            image = data.image
                        )
                    )
                }.onSuccess {
                    val updated = data.copy(subscribed = false)
                    _uiState.update {
                        it.copy(podcastFetchState = Resource.Success(updated))
                    }
                }
            }
        }
    }

    fun updateSortByTabSelection(newSelection: String) {
        val current = _uiState.value.podcastFetchState
        if (current is Resource.Success) {
            val episodes = current.data.episodes
            val newList = when (newSelection) {
                "Latest" -> {
                    episodes.sortedByDescending { it.datePublished }
                }
                "Oldest" -> {
                    episodes.sortedBy { it.datePublished }
                }
                else -> {episodes}
            }
            _uiState.update {
                it.copy(
                    sortByTabSelection = newSelection,
                    podcastFetchState = Resource.Success(current.data.copy(episodes = newList))
                )
            }
        }
    }
}