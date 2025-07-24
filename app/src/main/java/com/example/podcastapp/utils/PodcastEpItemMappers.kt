package com.example.podcastapp.utils

import com.example.podcastapp.data.local.entities.EpisodeHistoryEntity
import com.example.podcastapp.ui.viewmodel.PodcastEpItem

fun PodcastEpItem.toEpisodeHistoryEntity(): EpisodeHistoryEntity {
    return EpisodeHistoryEntity(
        image                 = this.image,
        title                 = this.title,
        datePublishedPretty   = this.datePublishedPretty,
        datePublished         = this.datePublished,
        episodeName           = this.episodeName,
        description           = this.description,
        enclosureUrl          = this.enclosureUrl,
        timeLeft              = this.timeLeft,
        progress              = this.progress,
        podcastId             = this.podcastId,
        episodeId             = this.episodeId,
        played                = this.played,
        playedAtMillis        = System.currentTimeMillis()
    )
}

fun EpisodeHistoryEntity.toPodcastEpItem(): PodcastEpItem {
    return PodcastEpItem(
        image                 = this.image,
        title                 = this.title,
        datePublishedPretty   = this.datePublishedPretty,
        datePublished         = this.datePublished,
        episodeName           = this.episodeName,
        description           = this.description,
        enclosureUrl          = this.enclosureUrl,
        timeLeft              = this.timeLeft,
        progress              = this.progress,
        podcastId             = this.podcastId,
        episodeId             = this.episodeId,
        played                = this.played
    )
}