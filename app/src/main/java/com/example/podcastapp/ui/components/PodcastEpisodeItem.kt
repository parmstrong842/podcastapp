package com.example.podcastapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.ui.screens.SIDE_PADDING
import com.example.podcastapp.ui.viewmodel.PodcastEpItem

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PodcastEpisodeItem(
    pod: PodcastEpItem,
    playMedia: (PodcastEpItem) -> Unit,
    navigateToEpisode: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = SIDE_PADDING, top = 8.dp, end = SIDE_PADDING)
            .clickable { navigateToEpisode(pod.episodeId) }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                model = pod.image,
                contentDescription = "episode image",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                loading = placeholder(R.drawable.ic_launcher_background),
                failure = placeholder(R.drawable.ic_launcher_foreground)
            )
            Column {
                Text(
                    text = pod.title,
                    fontSize = 16.sp
                )
                Text(
                    text = pod.datePublishedPretty,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        Text(
            text = pod.episodeName,
            fontSize = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = pod.description,
            fontSize = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayTimerButton(
                timeLeft = pod.timeLeft,
                progress = pod.progress,
                played = pod.played,
                onClick = { playMedia(pod) }
            )
            Icon(imageVector = Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "playlist add")
            Icon(imageVector = Icons.Default.Download, contentDescription = "Download")
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}