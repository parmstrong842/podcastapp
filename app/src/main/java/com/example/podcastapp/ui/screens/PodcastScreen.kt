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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.ui.components.PodcastEpisodeItem
import com.example.podcastapp.ui.theme.PodcastAppTheme
import com.example.podcastapp.ui.viewmodel.AppViewModelProvider
import com.example.podcastapp.ui.viewmodel.PodcastEpItem
import com.example.podcastapp.ui.viewmodel.PodcastFetchState
import com.example.podcastapp.ui.viewmodel.PodcastUiState
import com.example.podcastapp.ui.viewmodel.PodcastViewModel
import com.example.podcastapp.utils.Resource
import com.google.common.collect.Multimaps.index

@Composable
fun PodcastScreen(
    viewModel: PodcastViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit,
    playMedia: (PodcastEpItem) -> Unit,
    navigateToEpisode: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    PodcastScreenUI(
        uiState = uiState,
        navigateBack = navigateBack,
        playMedia = playMedia,
        onClickSubscribe = { subscribed ->
            if (subscribed) {
                viewModel.unsubscribeToPodcast()
            } else {
                viewModel.subscribeToPodcast()
            }
        },
        navigateToEpisode = navigateToEpisode,
        updateSortByTabSelection = { viewModel.updateSortByTabSelection(it) }
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PodcastScreenUI(
    uiState: PodcastUiState,
    navigateBack: () -> Unit,
    playMedia: (PodcastEpItem) -> Unit,
    onClickSubscribe: (Boolean) -> Unit,
    navigateToEpisode: (Long) -> Unit,
    updateSortByTabSelection: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PodcastScreenTopBar(navigateBack)
        Box {
            when (val state = uiState.podcastFetchState) {
                is Resource.Success -> Success(
                    image = state.data.image,
                    title = state.data.title,
                    subscribed = state.data.subscribed,
                    sortByTabSelection = uiState.sortByTabSelection,
                    episodes = state.data.episodes,
                    playMedia = playMedia,
                    onClickSubscribe = onClickSubscribe,
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
    sortByTabSelection: String,
    episodes: List<PodcastEpItem>,
    playMedia: (PodcastEpItem) -> Unit,
    onClickSubscribe: (Boolean) -> Unit,
    navigateToEpisode: (Long) -> Unit,
    updateSortByTabSelection: (String) -> Unit
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
                PodcastEpisodeItem(it, playMedia, navigateToEpisode)
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
    sortByTabSelection: String,
    updateSortByTabSelection: (String) -> Unit
) {
    val openSortByDialog = remember { mutableStateOf(false) }

    Row(modifier = modifier) {
        Button(
            onClick = { openSortByDialog.value = true },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(16.dp, 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(sortByTabSelection)
                Spacer(Modifier.padding(2.dp))
                Canvas(Modifier
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
    selectedOption: String,
    onBack: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onBack
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            val radioOptions = listOf("Latest", "Oldest", "Popular")
            Column(Modifier.selectableGroup()) {
                radioOptions.forEach { text ->
                    Row(
                        Modifier
                            .height(40.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = {
                                    onOptionSelected(text)
                                    onBack()
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        )
                        RadioButton(
                            selected = (text == selectedOption),
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
    FilterTabs(sortByTabSelection = "Latest", updateSortByTabSelection = {})
}

@Preview
@Composable
private fun SortByTabDialogPreview() {
    SortByTabDialog(selectedOption = "Latest", onBack = {}) { }
}


@Preview(showBackground = true)
@Composable
private fun PodcastScreenUIPreview() {
    val uiState = PodcastUiState(
        sortByTabSelection = "Latest",
        podcastFetchState = Resource.Success(
            PodcastFetchState(
                title = "The Joe Rogan Experience",
                image = "https://megaphone.imgix.net/podcasts/8e5bcebc-ca16-11ee-89f0-0fa0b9bdfc7c/image/11f568857987283428d892402e623b21.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
                subscribed = false,
                episodes = emptyList()

            )
        )
    )
    PodcastAppTheme {
        PodcastScreenUI(uiState, navigateBack = {}, playMedia = {}, onClickSubscribe = {}, navigateToEpisode = {}, updateSortByTabSelection = {})
    }
}
