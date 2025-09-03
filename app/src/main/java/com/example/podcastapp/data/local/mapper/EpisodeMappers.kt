package com.example.podcastapp.data.local.mapper

import com.example.podcastapp.data.local.model.EpisodeWithState
import com.example.podcastapp.ui.components.PodcastEpItem

fun EpisodeWithState.toPodcastEpItem(): PodcastEpItem {
    val timeLeft = (duration - position).coerceAtLeast(0L)
    val progress = if (duration > 0 && position >= 50) position.toFloat() / duration else 0f

    return PodcastEpItem(
        podcastTitle = podcastTitle,
        podcastImage = podcastImage,
        pubDate = pubDate,
        episodeTitle = episodeTitle,
        episodeImage = episodeImage,
        episodeDescription = episodeDescription,
        enclosureUrl = enclosureUrl,
        timeLeft = formatTimeMs(timeLeft),
        progress = progress,
        feedUrl = feedUrl,
        guid = guid,
        finished = finished
    )
}

fun formatTimeMs(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}