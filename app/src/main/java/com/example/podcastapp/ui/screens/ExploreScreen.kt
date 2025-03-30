package com.example.podcastapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.ui.viewmodel.AppViewModelProvider
import com.example.podcastapp.R
import com.example.podcastapp.audiocontroller.AudioControllerManager
import com.example.podcastapp.ui.viewmodel.ExploreViewModel
import com.example.podcastapp.ui.viewmodel.PodcastEpItem
import com.example.podcastapp.ui.viewmodel.PodcastSquare
import kotlinx.coroutines.launch

val SIDE_PADDING = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navigateToSearch: () -> Unit,
    playMedia: (PodcastEpItem) -> Unit,
    navigateToEpisode: (Long) -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    Column {
        PodcastTopBar(text = "Explore")
        PodcastSearchBar(navigateToSearch)
        val titles = listOf("For you", "News", "Business", "Arts", "Comedy", "Tech")
        val pagerState = rememberPagerState(pageCount = { titles.size })
        val coroutineScope = rememberCoroutineScope()
        SecondaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 8.dp
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            Column {
                Text(
                    modifier = Modifier.padding(horizontal = SIDE_PADDING, vertical = 20.dp),
                    text = "Popular & trending",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
                LazyRow(
                    modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.list) {
                        PodcastSquare(it)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                LazyColumn {
                    items(uiState.items) {
                        PodcastItem(it, playMedia, navigateToEpisode)
                    }
                }
            }
        }
    }
}

@Composable
private fun PodcastSearchBar(navigateToSearch: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SIDE_PADDING),
        onClick = navigateToSearch,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, Color.LightGray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.padding(16.dp),
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
            Text(
                text = "Search",
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PodcastItem(
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
                    text = pod.uploadDate,
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
            Button(onClick = { playMedia(pod) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayCircleOutline, contentDescription = "Play")
                    Text(text = pod.timeLeft)
                }
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "playlist add")
            Icon(imageVector = Icons.Default.Download, contentDescription = "Download")
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun PodcastSquare(pod: PodcastSquare) {
    val SIZE = 100.dp
    Column {
        Image(
            painter = painterResource(id = pod.imageResID),
            contentDescription = "Subscribe to podcast",
            modifier = Modifier
                .size(SIZE)
                .clip(RoundedCornerShape(8.dp))
                .drawWithContent {
                    drawContent()
                    whiteCircle()
                }
        )
        Text(
            text = pod.title,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .width(SIZE),
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
        Text(
            text = pod.publisher,
            modifier = Modifier
                .width(SIZE),
            color = Color.Gray,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

private fun ContentDrawScope.whiteCircle() {
    val x = size.width * 0.75f
    val y = size.height * 0.75f
    val radius = 14.dp.toPx()
    val lineLength = 6.dp.toPx()
    val strokeWidth = 1.dp.toPx()
    drawCircle(Color.White, radius, Offset(x, y))
    drawLine(
        Color.Blue,
        Offset(x - lineLength, y),
        Offset(x + lineLength, y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        Color.Blue,
        Offset(x, y - lineLength),
        Offset(x, y + lineLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun PodcastSquarePreview() {
    val pod = PodcastSquare(
        imageResID = R.drawable.ic_launcher_background,
        title = "Today, Explained",
        publisher = "Vox"
    )
    PodcastSquare(pod)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun PodcastItemPreview() {
    val item = PodcastEpItem(
        image = "",
        title = "TED Talks Daily",
        uploadDate = "Yesterday",
        episodeName = "The science of friction -- and its surprising impact on our lives | Jennifer Person",
        description = "Join the Acquired Limited Partner program! http://siteahsiethaisetha (works best on mobile)",
        timeLeft = "1hr 2min",
        enclosureUrl = "",
        podcastId = 1,
        episodeId = 1
    )
    PodcastItem(item, {}) {}
}