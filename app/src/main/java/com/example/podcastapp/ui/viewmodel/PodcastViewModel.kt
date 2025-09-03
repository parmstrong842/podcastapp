package com.example.podcastapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.data.local.DatabaseRepository
import com.example.podcastapp.data.remote.RemoteRepository
import com.example.podcastapp.ui.components.PodcastEpItem
import com.example.podcastapp.utils.Resource
import com.prof18.rssparser.model.RssItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


private const val tag = "PodcastViewModel"

data class PodcastFetchState(
    val podcastTitle: String,
    val podcastImage: String,
    val episodes: List<PodcastEpItem>,
    val subscribed: Boolean
)

data class PodcastUiState(
    val sortByTabSelection: String,
    val podcastFetchState: Resource<PodcastFetchState>
)

class PodcastViewModel(
    private val feedUrl: String,
    private val remoteRepository: RemoteRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<PodcastUiState> = MutableStateFlow(PodcastUiState(
        sortByTabSelection = "Latest",
        podcastFetchState = Resource.Loading
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val podcastFetchResult = try {
                val channel = remoteRepository.getRssChannel(feedUrl)

                val podcastTitle = channel.title.orEmpty()
                val podcastImage = channel.itunesChannelData?.image
                        ?: channel.image?.url.orEmpty()

                val episodes = channel.items.map { it.toPodcastEpItem(podcastTitle, podcastImage, feedUrl) }

                Resource.Success(
                    PodcastFetchState(
                        podcastTitle = podcastTitle,
                        podcastImage = podcastImage,
                        subscribed = databaseRepository.isSubscribed(feedUrl) == true,
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

    private fun RssItem.toPodcastEpItem(
        podcastTitle: String,
        podcastImage: String,
        feedUrl: String
    ): PodcastEpItem {
        val epImage =
            this.itunesItemData?.image
                ?: this.image
                ?: podcastImage

        val description = when {
            !this.content.isNullOrBlank() -> this.content!!
            !this.itunesItemData?.summary.isNullOrBlank() -> this.itunesItemData!!.summary!!
            else -> this.description.orEmpty()
        }

        val audioUrl = this.audio ?: "" // TODO: handle case where there is no audio

        val duration = parseItunesDuration(this.itunesItemData?.duration)

        val stableGuid = when {
            !this.guid.isNullOrBlank() -> this.guid!!
            else -> java.security.MessageDigest.getInstance("SHA-256")
                .digest((feedUrl + title.orEmpty() + pubDate.orEmpty()).toByteArray())
                .joinToString("") { "%02x".format(it) }
        }

        return PodcastEpItem(
            podcastTitle = podcastTitle,
            podcastImage = podcastImage,
            pubDate = this.pubDate.orEmpty(),
            episodeTitle = this.title.orEmpty(),
            episodeImage = epImage,
            episodeDescription = description,
            enclosureUrl = audioUrl,
            timeLeft = formatTime(duration),
            progress = 0f,
            feedUrl = feedUrl,
            guid = stableGuid,
            finished = false
        )
    }

    private fun parseItunesDuration(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        return try {
            val p = raw.split(":").map { it.toLong() }
            when (p.size) {
                1 -> p[0]                                   // SS
                2 -> p[0] * 60 + p[1]                       // MM:SS
                3 -> p[0] * 3600 + p[1] * 60 + p[2]         // HH:MM:SS
                else -> 0L
            }
        } catch (_: Exception) { 0L }
    }

    private fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    fun subscribeToPodcast() {
        viewModelScope.launch {
            val current = _uiState.value.podcastFetchState
            if (current is Resource.Success) {
                val data = current.data
                runCatching {
                    databaseRepository.subscribe(
                        feedUrl = feedUrl,
                        podcastTitle = data.podcastTitle,
                        image = data.podcastImage.takeIf { it.isNotEmpty() }
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
                    databaseRepository.unsubscribe(
                        feedUrl = feedUrl
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

    class Factory(
        private val feedUrl: String,
        private val application: PodcastApplication
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val remoteRepository = application.container.remoteRepository
            val databaseRepository = application.container.databaseRepository
            return PodcastViewModel(feedUrl, remoteRepository, databaseRepository) as T
        }
    }
}