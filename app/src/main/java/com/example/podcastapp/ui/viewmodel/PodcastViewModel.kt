package com.example.podcastapp.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.local.entities.SubscribedPodcastEntity
import com.example.podcastapp.data.local.entities.progressFraction
import com.example.podcastapp.data.local.entities.timeLeftMs
import com.example.podcastapp.data.remote.RemoteRepository
import com.example.podcastapp.ui.components.PodcastEpItem
import com.example.podcastapp.utils.Resource
import com.example.podcastapp.utils.formatTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale


private const val tag = "PodcastViewModel"

data class PodcastFetchState(
    val title: String,
    val image: String,
    val episodes: List<PodcastEpItem>,
    val subscribed: Boolean
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

    private val encodedUrl: String = checkNotNull(savedStateHandle["feedUrl"])
    private val feedUrl = Uri.decode(encodedUrl)

    private val _uiState: MutableStateFlow<PodcastUiState> = MutableStateFlow(PodcastUiState(
        sortByTabSelection = "Latest",
        podcastFetchState = Resource.Loading
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val podcastFetchResult = try {
                val item = PodcastEpItem(
                    "https://megaphone.imgix.net/podcasts/6b48647a-f635-11ef-8324-2b180a017350/image/d142ec926f025bd64b32d9a2e96aa81a.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
                    "The Joe Rogan Experience",
                    "date",
                    "Kalimba",
                    "#2282 - Bill Murray",
                    "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
                    "1(00:00:00)",
                    0.5f,
                    "feedUrl",
                    1,
                    false,
                    false
                )
//                val podcastResponse = remoteRepository.podcastByFeedID(podcastId)
//                val response = remoteRepository.episodesByFeedID(podcastId)
//                val allProgress = databaseRepository.getAllProgressForPodcast(podcastId)
//                val episodes = response.items.map {
//                    val progress = allProgress.find { p -> p.guid == it.id } // TODO:
//                    val remainingMs = progress?.timeLeftMs() ?: (it.duration.toLong() * 1000L)
//                    val timeLeft = formatTime(remainingMs)
//                    PodcastEpItem(
//                        image = it.image,
//                        podcastTitle = podcastResponse.feed.title,
//                        pubDate = it.datePublishedPretty,
//                        episodeName = it.title,
//                        description = it.description,
//                        enclosureUrl = it.enclosureUrl,
//                        timeLeft = timeLeft,
//                        progress = progress?.progressFraction() ?: 0f,
//                        feedUrl = podcastId,
//                        guid = it.id,
//                        played = progress?.finished == true
//                    )
//                }
                Resource.Success(
                    PodcastFetchState(
                        title = "The Joe Rogan Experience",
                        image = "https://megaphone.imgix.net/podcasts/6b48647a-f635-11ef-8324-2b180a017350/image/d142ec926f025bd64b32d9a2e96aa81a.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
                        subscribed = databaseRepository.getSubscription(feedUrl) != null,
                        episodes = listOf(item)

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
                            title = data.title,
                            image = data.image,
                            feedUrl = feedUrl
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
                            title = data.title,
                            image = data.image,
                            feedUrl = feedUrl
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

    fun enqueue(item: PodcastEpItem) {
        viewModelScope.launch {
            databaseRepository.enqueue(item)
        }
    }

    fun removeFromQueue(item: PodcastEpItem) {
        viewModelScope.launch {
            databaseRepository.remove("${item.feedUrl}#${item.guid}")
        }
    }

    // TODO: need to be able to parse different date formats
    fun updateSortByTabSelection(newSelection: String) {
        val current = _uiState.value.podcastFetchState
        if (current is Resource.Success) {
            val episodes = current.data.episodes
            // TODO: private rss feeds cant be sorted by popular
            val newList = when (newSelection) {
                "Latest" -> {
                    episodes.sortedByDescending { parseDate(it.pubDate) }
                }
                "Oldest" -> {
                    episodes.sortedBy { parseDate(it.pubDate) }
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

    private fun parseDate(dateString: String): Long {
        val formats = listOf(
            DateTimeFormatter.RFC_1123_DATE_TIME,         // RFC 822 format (e.g., "Tue, 02 Jun 2025 14:30:00 GMT")
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,       // RFC 3339 format (e.g., "2025-06-02T14:30:00Z")
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,        // Basic ISO format (e.g., "2025-06-02T14:30:00")
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        )

        for (formatter in formats) {
            try {
                val date = ZonedDateTime.parse(dateString, formatter)
                return date.toInstant().toEpochMilli()
            } catch (e: DateTimeParseException) {
                Log.d(tag, "Failed to parse with format $formatter.: ${e.message}")
            }
        }

        Log.d(tag, "All date formats failed. Returning default value.")
        return 0L
    }
}