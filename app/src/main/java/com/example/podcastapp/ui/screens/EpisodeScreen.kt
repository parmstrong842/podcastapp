package com.example.podcastapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.audiocontroller.IAudioController
import com.example.podcastapp.ui.viewmodel.EpisodeViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EpisodeScreen(
    viewModel: EpisodeViewModel,
    navigateBack: () -> Unit,
    audioController: IAudioController
) {
    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()
    Column{
        EpisodeTopBar(navigateBack)
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                model = uiState.image,
                contentDescription = "episode image",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(50.dp),
                loading = placeholder(R.drawable.ic_launcher_background),
                failure = placeholder(R.drawable.ic_launcher_foreground)
            )
            IconButton(
                onClick = {
                    if (audioController.shouldShowPlayButton) {
                        audioController.resumePlayback()
                    } else {
                        audioController.pauseMedia()
                    }
                },
            ) {
                if (audioController.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (audioController.shouldShowPlayButton) {
                            Icons.Filled.PlayArrow
                        } else {
                            Icons.Filled.Pause
                        },
                        contentDescription = if (audioController.shouldShowPlayButton) "Play" else "Pause",
                        tint = Color.White
                    )
                }
            }
            Text(text = "${uiState.datePublishedPretty} - ${uiState.duration}")
            Text(uiState.title)
            Text(text = uiState.description)
        }
    }
}

@Composable
private fun EpisodeTopBar(
    navigateBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = navigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
        }
    }
}
