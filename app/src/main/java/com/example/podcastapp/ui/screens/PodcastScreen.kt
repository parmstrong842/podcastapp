package com.example.podcastapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.ui.theme.PodcastAppTheme
import com.example.podcastapp.ui.viewmodel.AppViewModelProvider
import com.example.podcastapp.ui.viewmodel.PodcastUiState
import com.example.podcastapp.ui.viewmodel.PodcastViewModel
import com.example.podcastapp.ui.viewmodel.SearchViewModel

@Composable
fun PodcastScreen(
    viewModel: PodcastViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    PodcastScreenUI(
        uiState = uiState,
        navigateBack = navigateBack
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PodcastScreenUI(
    uiState: PodcastUiState,
    navigateBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PodcastScreenTopBar(navigateBack)
        GlideImage(
            model = uiState.image,
            contentDescription = "subscription image",
            modifier = Modifier
                .padding(end = 8.dp)
                .size(100.dp)
                .clip(RoundedCornerShape(6.dp)),
            loading = placeholder(R.drawable.ic_launcher_background),
            failure = placeholder(R.drawable.ic_launcher_foreground)
        )
        Text(
            text = uiState.title,
            fontSize = 24.sp
        )
        LazyColumn {
            items(uiState.episodes) {
                PodcastItem(it)
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

@Preview(showBackground = true)
@Composable
private fun PodcastScreenUIPreview() {
    val uiState = PodcastUiState(
        title = "The Joe Rogan Experience",
        image = "https://megaphone.imgix.net/podcasts/8e5bcebc-ca16-11ee-89f0-0fa0b9bdfc7c/image/11f568857987283428d892402e623b21.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
        episodes = emptyList()
    )
    PodcastAppTheme {
        PodcastScreenUI(uiState, navigateBack = {})
    }
}
