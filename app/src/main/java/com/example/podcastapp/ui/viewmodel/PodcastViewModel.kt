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
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


private const val tag = "PodcastViewModel"

data class PodcastFeed(
    val podcastTitle: String,
    val podcastImage: String,
    val episodes: List<PodcastEpItem>
)

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
                val parser = RssParserBuilder().build()
                val channel: RssChannel = parser.getRssChannel(feedUrl)

                val podcastTitle = channel.title.orEmpty()
                val podcastImage = channel.itunesChannelData?.image
                        ?: channel.image?.url.orEmpty()

                val episodes = channel.items.map { it.toPodcastEpItem(podcastTitle, podcastImage, feedUrl) }
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
                        podcastTitle = podcastTitle,
                        podcastImage = podcastImage,
                        subscribed = databaseRepository.isSubscribed(feedUrl) != null,
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

        val duration = this.itunesItemData?.duration.orEmpty()

        return PodcastEpItem(
            podcastTitle = podcastTitle,
            podcastImage = podcastImage,
            pubDate = this.pubDate.orEmpty(),
            episodeTitle = this.title.orEmpty(),
            episodeImage = epImage,
            episodeDescription = description,
            enclosureUrl = audioUrl,
            timeLeft = normalizeDurationToHHMMSS(duration), // same helper you already have
            progress = 0f,                 // fill from DB later
            feedUrl = feedUrl,
            guid = this.guid.orEmpty(),
            finished = false               // fill from DB later
        )
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