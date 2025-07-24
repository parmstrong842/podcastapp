package com.example.podcastapp.ui.screens

import android.R.attr.top
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.ui.components.PodcastEpisodeItem
import com.example.podcastapp.ui.theme.Dimens
import com.example.podcastapp.ui.viewmodel.AppViewModelProvider
import com.example.podcastapp.ui.viewmodel.DownloadsViewModel
import com.example.podcastapp.ui.viewmodel.HistoryViewModel
import com.example.podcastapp.ui.viewmodel.PodcastEpItem
import com.example.podcastapp.ui.viewmodel.QueueViewModel
import com.example.podcastapp.ui.viewmodel.SubscriptionsViewModel
import com.example.podcastapp.ui.viewmodel.UserUiState
import com.example.podcastapp.ui.viewmodel.UserViewModel
import com.example.podcastapp.utils.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    playMedia: (PodcastEpItem) -> Unit,
    navigateToPodcast: (Int) -> Unit,
    navigateToEpisode: (Long) -> Unit,
    viewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val titles = listOf("Your queue", "Downloads", "History", "Subscriptions")
    val pagerState = rememberPagerState(pageCount = { titles.size })
    val scope = rememberCoroutineScope()

    Column {
        PodcastTopBar(
            text = "Activity",
            menuItems = listOf(R.string.import_subscriptions_from_opml),
            onMenuItemClick = {
                when (it) {
                    R.string.import_subscriptions_from_opml -> {} // TODO: import subscriptions
                }
            }
        )
        SecondaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = Dimens.playerCollapsedHeight),
        ) { pageIndex ->
            when (pageIndex) {
                0 -> QueueContent()
                1 -> DownloadsContent()
                2 -> HistoryContent(playMedia, navigateToEpisode)
                3 -> SubscriptionsContent(navigateToPodcast = navigateToPodcast)
                else -> Text("Invalid Page: $pageIndex")
            }
        }
    }
}

@Composable
fun QueueContent(viewModel: QueueViewModel = viewModel(factory = AppViewModelProvider.Factory)) {

}

@Composable
fun DownloadsContent(viewModel: DownloadsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {

}

@Composable
fun HistoryContent(
    playMedia: (PodcastEpItem) -> Unit,
    navigateToEpisode: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Box {
        when (val state = uiState.historyFetchState) {
            is Resource.Success -> {
                LazyColumn {
                    items(state.data.history) {
                        PodcastEpisodeItem(it, playMedia, navigateToEpisode)
                    }
                }
            }
            is Resource.Loading -> {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .padding(top = 32.dp)
                )
            }
            is Resource.Error -> {
                Text(
                    text = "Error",
                )
            }
            is Resource.Idle -> {
                Text(
                    text = "Idle",
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SubscriptionsContent(
    viewModel: SubscriptionsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToPodcast: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn {
        items(uiState.subscriptions) {
            Row(
                modifier = Modifier.clickable { navigateToPodcast(it.id) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlideImage(
                    model = it.image,
                    contentDescription = "subscription image",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(50.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    loading = placeholder(R.drawable.ic_launcher_background),
                    failure = placeholder(R.drawable.ic_launcher_foreground)
                )
                Text(it.title)
            }
        }
    }
}