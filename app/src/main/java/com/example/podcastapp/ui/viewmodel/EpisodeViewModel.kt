package com.example.podcastapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.remote.RemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException


private const val tag = "EpisodeViewModel"

data class EpisodeUiState(
    val title: String,
    val image: String?,
    val description: String,
    val datePublishedPretty: String,
    val duration: Int
)

// TODO: update to use rss feeds
class EpisodeViewModel(
    private val guid: String,
    private val remoteRepository: RemoteRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<EpisodeUiState> = MutableStateFlow(EpisodeUiState(
        title = "",
        image = "",
        description = "",
        datePublishedPretty = "",
        duration = 0
    ))
    val uiState = _uiState.asStateFlow()

    init {
        Log.d(tag, guid.toString())
        viewModelScope.launch {
            try {
//                val response = remoteRepository.episodeByID(episodeId)
//                _uiState.update {
//                    it.copy(
//                        image = response.episode.image,
//                        title = response.episode.title,
//                        description = response.episode.description,
//                        datePublishedPretty = response.episode.datePublishedPretty,
//                        duration = response.episode.duration
//                    )
//                }
//                // TODO:
            } catch (e: HttpException) { // TODO: handle exceptions
                throw e
            }
        }
    }

    class Factory(
        private val guid: String,
        private val application: PodcastApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val remoteRepository = application.container.remoteRepository
            val databaseRepository = application.container.databaseRepository
            return EpisodeViewModel(guid, remoteRepository, databaseRepository) as T
        }
    }
}
