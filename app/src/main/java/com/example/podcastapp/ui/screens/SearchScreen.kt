package com.example.podcastapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.data.remote.models.podcastindex.Podcast
import com.example.podcastapp.ui.viewmodel.AppViewModelProvider
import com.example.podcastapp.ui.viewmodel.SearchResult
import com.example.podcastapp.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit,
    navigateToPodcast: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { navigateBack() },
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to explore"
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = uiState.query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("What do you want to listen to?") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    }
                )
            )
        }
        when (val state = uiState.searchResult) {
            is SearchResult.Success -> Success(
                listState = listState,
                podcasts = state.podcasts,
                navigateToPodcast = navigateToPodcast,
            )
            is SearchResult.Error -> Error()
            is SearchResult.Loading -> Loading(Modifier.align(Alignment.CenterHorizontally))
            is SearchResult.Idle -> Idle()
        }
    }
}

@Composable
private fun Success(
    listState: LazyListState,
    podcasts: List<Podcast>,
    navigateToPodcast: (Int) -> Unit,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(podcasts) { podcast ->
            PodcastItem(
                podcast = podcast,
                onClick = { navigateToPodcast(podcast.id) },
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PodcastItem(
    modifier: Modifier = Modifier,
    podcast: Podcast,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                model = podcast.image,
                contentDescription = podcast.title,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp)),
                loading = placeholder(R.drawable.ic_launcher_background),
                failure = placeholder(R.drawable.ic_launcher_foreground)
            )
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = podcast.title,
                    fontSize = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = podcast.author,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun Error() {
    Text(text = "Something went wrong.")
}

@Composable
private fun Loading(modifier: Modifier) {
    CircularProgressIndicator(
        modifier
            .padding(top = 32.dp)
    )
}

@Composable
fun Idle() {}

//@Preview(
//    showBackground = true,
//    backgroundColor = 0xFFFFFF,
//    widthDp = 412
//)
//@Composable
//private fun PodcastItemPreview() {
//    val pod = Podcast(
//        id = 75075,
//        title = "Batman University",
//        description = "Batman University is a seasonal podcast about you know who. It began with an analysis of episodes of “Batman: The Animated Series” but has now expanded to cover other series, movies, and media. Your professor is Tony Armstrong.",
//        image = "https://www.theincomparable.com/imgs/logos/logo-batmanuniversity-3x.jpg",
//        url = "https://feeds.theincomparable.com/batmanuniversity",
//        author = "Tony Armstrong"
//    )
//    PodcastItem(podcast = pod, onClick = {}, subscribeToPodcast = {})
//}