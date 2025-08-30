package com.example.podcastapp.utils

//import com.example.podcastapp.data.local.entities.EpisodeHistoryEntity
//import com.example.podcastapp.data.local.entities.QueueEntity
//import com.example.podcastapp.ui.components.PodcastEpItem
//
//fun PodcastEpItem.toEpisodeHistoryEntity(): EpisodeHistoryEntity {
//    return EpisodeHistoryEntity(
//        image                 = this.image,
//        podcastTitle          = this.podcastTitle,
//        pubDate               = this.pubDate,
//        episodeName           = this.episodeName,
//        description           = this.description,
//        enclosureUrl          = this.enclosureUrl,
//        timeLeft              = this.timeLeft,
//        progress              = this.progress,
//        feedUrl               = this.feedUrl,
//        guid                  = this.guid,
//        played                = this.played,
//        enqueued              = this.enqueued,
//        playedAtMillis        = System.currentTimeMillis()
//    )
//}
//
//fun EpisodeHistoryEntity.toPodcastEpItem(): PodcastEpItem {
//    return PodcastEpItem(
//        image                 = this.image,
//        podcastTitle          = this.podcastTitle,
//        pubDate               = this.pubDate,
//        episodeName           = this.episodeName,
//        description           = this.description,
//        enclosureUrl          = this.enclosureUrl,
//        timeLeft              = this.timeLeft,
//        progress              = this.progress,
//        feedUrl               = this.feedUrl,
//        guid                  = this.guid,
//        played                = this.played,
//        enqueued              = this.enqueued
//    )
//}
//
//fun PodcastEpItem.toQueueEntity(next: Int): QueueEntity {
//    return QueueEntity(
//        key                   = "$feedUrl#$guid",
//        image                 = this.image,
//        podcastTitle          = this.podcastTitle,
//        pubDate               = this.pubDate,
//        episodeName           = this.episodeName,
//        description           = this.description,
//        enclosureUrl          = this.enclosureUrl,
//        timeLeft              = this.timeLeft,
//        progress              = this.progress,
//        feedUrl               = this.feedUrl,
//        guid                  = this.guid,
//        played                = this.played,
//        enqueued              = true,
//        queuePosition         = next
//    )
//}
//
//fun QueueEntity.toPodcastEpItem(): PodcastEpItem {
//    return PodcastEpItem(
//        image                 = this.image,
//        podcastTitle          = this.podcastTitle,
//        pubDate               = this.pubDate,
//        episodeName           = this.episodeName,
//        description           = this.description,
//        enclosureUrl          = this.enclosureUrl,
//        timeLeft              = this.timeLeft,
//        progress              = this.progress,
//        feedUrl               = this.feedUrl,
//        guid                  = this.guid,
//        played                = this.played,
//        enqueued              = false
//    )
//}