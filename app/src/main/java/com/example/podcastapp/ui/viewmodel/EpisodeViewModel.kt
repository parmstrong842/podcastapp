package com.example.podcastapp.ui.viewmodel

import android.util.Log
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


private const val tag = "EpisodeViewModel"

data class EpisodeUiState(
    val title: String,
    val image: String?,
    val description: String,
)

class EpisodeViewModel(
    savedStateHandle: SavedStateHandle,
    private val databaseRepository: DatabaseRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val episodeId: Long = checkNotNull(savedStateHandle["episodeId"])

    private val _uiState: MutableStateFlow<EpisodeUiState> = MutableStateFlow(EpisodeUiState(
        title = "",
        image = "",
        description = ""
    ))
    val uiState = _uiState.asStateFlow()

    init {
        Log.d(tag, episodeId.toString())
        viewModelScope.launch {
            try {
                val response = remoteRepository.episodeByID(episodeId)
                _uiState.update {
                    it.copy(
                        image = response.episode.image,
                        title = response.episode.title,
                        description = response.episode.description
                    )
                }
                // TODO:
            } catch (e: HttpException) { // TODO: handle exceptions
                throw e
            }
        }
    }
}
