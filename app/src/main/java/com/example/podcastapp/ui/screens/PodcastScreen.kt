package com.example.podcastapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.ui.components.PodcastEpItem
import com.example.podcastapp.ui.components.PodcastEpisodeCard
import com.example.podcastapp.ui.theme.PodcastAppTheme
import com.example.podcastapp.ui.viewmodel.PodcastFetchState
import com.example.podcastapp.ui.viewmodel.PodcastUiState
import com.example.podcastapp.ui.viewmodel.PodcastViewModel
import com.example.podcastapp.ui.viewmodel.SortOrder
import com.example.podcastapp.util.Resource

// TODO: add episode search
// TODO: add episode filters (played, unplayed, downloaded)

@Composable
fun PodcastScreen(
    viewModel: PodcastViewModel,
    isPlaying: Boolean,
    nowPlayingGuid: String?,
    navigateBack: () -> Unit,
    playMedia: (PodcastEpItem) -> Unit,
    navigateToEpisode: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    PodcastScreenUI(
        uiState = uiState,
        isPlaying = isPlaying,
        nowPlayingGuid = nowPlayingGuid,
        navigateBack = navigateBack,
        playMedia = playMedia,
        onClickSubscribe = { subscribed ->
            if (subscribed) {
                viewModel.unsubscribeToPodcast()
            } else {
                viewModel.subscribeToPodcast()
            }
        },
        onClickQueue = {
//            if (it.enqueued) {
//                viewModel.removeFromQueue(it)
//            } else {
//                viewModel.enqueue(it)
//            }
        },
        navigateToEpisode = navigateToEpisode,
        updateSortByTabSelection = { viewModel.updateSortByTabSelection(it) }
    )
}

@Composable
fun PodcastScreenUI(
    uiState: PodcastUiState,
    isPlaying: Boolean,
    nowPlayingGuid: String?,
    navigateBack: () -> Unit,
    playMedia: (PodcastEpItem) -> Unit,
    onClickSubscribe: (Boolean) -> Unit,
    onClickQueue: (PodcastEpItem) -> Unit,
    navigateToEpisode: (String) -> Unit,
    updateSortByTabSelection: (SortOrder) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PodcastScreenTopBar(navigateBack)
        Box {
            when (val state = uiState.podcastFetchState) {
                is Resource.Success -> Success(
                    image = state.data.podcastImage,
                    title = state.data.podcastTitle,
                    subscribed = state.data.subscribed,
                    sortByTabSelection = uiState.sortByTabSelection,
                    isPlaying = isPlaying,
                    nowPlayingGuid = nowPlayingGuid,
                    episodes = state.data.episodes,
                    playMedia = playMedia,
                    onClickSubscribe = onClickSubscribe,
                    onClickQueue = onClickQueue,
                    navigateToEpisode = navigateToEpisode,
                    updateSortByTabSelection = updateSortByTabSelection
                )
                is Resource.Loading -> Loading(
                    Modifier.align(
                        Alignment.Center
                    )
                )
                is Resource.Error -> Error()
                Resource.Idle -> Idle()
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun Success(
    modifier: Modifier = Modifier,
    image: String,
    title: String,
    subscribed: Boolean,
    sortByTabSelection: SortOrder,
    isPlaying: Boolean,
    nowPlayingGuid: String?,
    episodes: List<PodcastEpItem>,
    playMedia: (PodcastEpItem) -> Unit,
    onClickSubscribe: (Boolean) -> Unit,
    onClickQueue: (PodcastEpItem) -> Unit,
    navigateToEpisode: (String) -> Unit,
    updateSortByTabSelection: (SortOrder) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlideImage(
            model = image,
            contentDescription = "subscription image",
            modifier = modifier
                .padding(end = 8.dp)
                .size(100.dp)
                .clip(RoundedCornerShape(6.dp)),
            loading = placeholder(R.drawable.ic_launcher_background),
            failure = placeholder(R.drawable.ic_launcher_foreground)
        )
        Text(
            text = title,
            fontSize = 24.sp
        )
        Button(onClick = { onClickSubscribe(subscribed) }) {
            Text(text = if (subscribed) "Unsubscribe" else "Subscribe")
        }
        FilterTabs(
            sortByTabSelection = sortByTabSelection,
            updateSortByTabSelection = updateSortByTabSelection
        )
        LazyColumn {
            items(episodes) {
                PodcastEpisodeCard(
                    pod = it,
                    isPlaying = isPlaying,
                    nowPlayingGuid = nowPlayingGuid,
                    playMedia = playMedia,
                    onClickQueue = {
                        onClickQueue(it)
                    },
                    navigateToEpisode = navigateToEpisode
                )
            }
        }
    }
}

@Composable
private fun PodcastScreenTopBar(
    navigateBack: () -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        IconButton(onClick = navigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
        }
    }
}

@Composable
private fun FilterTabs(
    modifier: Modifier = Modifier,
    sortByTabSelection: SortOrder,
    updateSortByTabSelection: (SortOrder) -> Unit
) {
    val openSortByDialog = remember { mutableStateOf(false) }

    Row(modifier = modifier) {
        Button(
            onClick = { openSortByDialog.value = true },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(16.dp, 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(sortByTabSelection.labelRes))
                Spacer(Modifier.padding(2.dp))
                Canvas(Modifier // TODO: make this work with dark mode
                    .size(10.dp)
                    .offset(y = 3.dp)) {
                    drawLine(Color.White, Offset(0f, 0f), center, 4f, StrokeCap.Round)
                    drawLine(Color.White, center, Offset(size.width, 0f), 4f, StrokeCap.Round)
                }
            }
        }
    }

    if (openSortByDialog.value) {
        SortByTabDialog(
            onBack = { openSortByDialog.value = false },
            onOptionSelected = updateSortByTabSelection,
            selectedOption = sortByTabSelection
        )
    }
}

@Composable
private fun SortByTabDialog(
    modifier: Modifier = Modifier,
    selectedOption: SortOrder,
    onBack: () -> Unit,
    onOptionSelected: (SortOrder) -> Unit
) {
    Dialog(
        onDismissRequest = onBack
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            val radioOptions = SortOrder.entries
            Column(Modifier.selectableGroup()) {
                radioOptions.forEach { sortOrder ->
                    Row(
                        Modifier
                            .height(40.dp)
                            .selectable(
                                selected = (sortOrder == selectedOption),
                                onClick = {
                                    onOptionSelected(sortOrder)
                                    onBack()
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(sortOrder.labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        )
                        RadioButton(
                            selected = (sortOrder == selectedOption),
                            onClick = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier
            .padding(top = 32.dp)
    )
}

@Composable
private fun Error(modifier: Modifier = Modifier) {
    Text(
        text = "Error",
        modifier = modifier
    )
}

@Composable
private fun Idle(modifier: Modifier = Modifier) {
    Text(
        text = "Idle",
        modifier = modifier
    )
}

@Preview
@Composable
private fun FilterTabPreview() {
    FilterTabs(sortByTabSelection = SortOrder.LATEST, updateSortByTabSelection = {})
}

@Preview
@Composable
private fun SortByTabDialogPreview() {
    SortByTabDialog(selectedOption = SortOrder.LATEST, onBack = {}) { }
}


@Preview(showBackground = true)
@Composable
private fun PodcastScreenUIPreview() {
    val uiState = PodcastUiState(
        sortByTabSelection = SortOrder.LATEST,
        podcastFetchState = Resource.Success(
            PodcastFetchState(
                podcastTitle = "The Joe Rogan Experience",
                podcastImage = "https://megaphone.imgix.net/podcasts/8e5bcebc-ca16-11ee-89f0-0fa0b9bdfc7c/image/11f568857987283428d892402e623b21.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
                subscribed = false,
                episodes = emptyList()
            )
        )
    )
    PodcastAppTheme {
        PodcastScreenUI(uiState, true, "123456", navigateBack = {}, playMedia = {}, onClickSubscribe = {}, navigateToEpisode = {}, updateSortByTabSelection = {}, onClickQueue = {})
    }
}
