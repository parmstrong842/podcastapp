package com.example.podcastapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.ui.viewmodel.AppViewModelProvider
import com.example.podcastapp.ui.viewmodel.EpisodeViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EpisodeScreen(
    navigateBack: () -> Unit,
    viewModel: EpisodeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()
    Column {
        EpisodeTopBar(navigateBack)
        Column(
            modifier = Modifier.verticalScroll(scrollState),
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
