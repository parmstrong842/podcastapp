package com.example.podcastapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.R
import com.example.podcastapp.ui.components.PodcastEpItem
import com.example.podcastapp.ui.components.PodcastEpisodeCard
import com.example.podcastapp.ui.components.PodcastTopBar
import com.example.podcastapp.ui.theme.Dimens.SIDE_PADDING
import com.example.podcastapp.ui.viewmodel.ExploreViewModel
import com.example.podcastapp.ui.viewmodel.PodcastSquare
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navigateToSearch: () -> Unit,
    playMedia: (PodcastEpItem) -> Unit,
    navigateToEpisode: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory(LocalContext.current.applicationContext as PodcastApplication))
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
                        //PodcastEpisodeCard(it, playMedia, navigateToEpisode)
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
        podcastTitle = "The Joe Rogan Experience",
        podcastImage = "https://megaphone.imgix.net/podcasts/6b48647a-f635-11ef-8324-2b180a017350/image/d142ec926f025bd64b32d9a2e96aa81a.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
        pubDate = "date",
        episodeTitle = "Kalimba",
        episodeImage = "https://megaphone.imgix.net/podcasts/6b48647a-f635-11ef-8324-2b180a017350/image/d142ec926f025bd64b32d9a2e96aa81a.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
        episodeDescription = "description",
        enclosureUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
        timeLeft = "30:00",
        progress = 0.5f,
        feedUrl = "feedUrl",
        guid = "1",
        finished = false
    )
    PodcastEpisodeCard(item, {}, {}) {}
}