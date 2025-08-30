package com.example.podcastapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.PodcastApplication
import com.example.podcastapp.R
import com.example.podcastapp.data.local.entity.PodcastEntity
import com.example.podcastapp.ui.theme.Dimens.SIDE_PADDING
import com.example.podcastapp.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(LocalContext.current.applicationContext as PodcastApplication)),
    navigateToPodcast: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenUI(
        subscriptions = uiState.subscriptions,
        navigateToPodcast = navigateToPodcast
    )
}

@Composable
fun HomeScreenUI(
    modifier: Modifier = Modifier,
    subscriptions: List<PodcastEntity>,
    navigateToPodcast: (String) -> Unit
) {
    Column(modifier.fillMaxSize()) {
        PodcastTopBar(text = "PodcastApp")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SIDE_PADDING),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subscriptions) {
                SubscriptionImage(
                    image = it.podcastImage,
                    navigateToPodcast = { navigateToPodcast(it.feedUrl) }
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SubscriptionImage(
    image: String?,
    navigateToPodcast: () -> Unit
) {
    GlideImage(
        model = image,
        contentDescription = "subscription image",
        modifier = Modifier
            .padding(end = 8.dp)
            .size(50.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { navigateToPodcast() },
        loading = placeholder(R.drawable.ic_launcher_background),
        failure = placeholder(R.drawable.ic_launcher_foreground)
    )
}

@Composable
fun PodcastTopBar(
    modifier: Modifier = Modifier,
    text: String,
    menuItems: List<Int> = emptyList(),
    onMenuItemClick: (Int) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(Icons.Default.Cast, "Cast")
        Text(
            text = text,
            fontSize = 24.sp,
        )
        if (menuItems.isNotEmpty()) {
            Box {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "more",
                    modifier = Modifier.clickable { expanded = !expanded }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    menuItems.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it)) },
                            onClick = {
                                onMenuItemClick(it)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "more",
            modifier = Modifier.clickable { expanded = !expanded }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenUIPreview() {

//    val subscriptions = listOf(
//        PodcastEntity("","https://image.simplecastcdn.com/images/4638a61f-6fe2-4fc0-9398-c6118a63c1d6/65ba0b3e-50c6-4d13-aa12-40a6873e255b/3000x3000/microsoftteams-image-5.png?aid=rss_feed", ""),
//        PodcastEntity("","https://image.simplecastcdn.com/images/84f7d3f2-4d0e-48c8-bcb0-8d047036d197/a11d3578-759a-4eab-85cb-3122acf0cf2c/3000x3000/uploads-2f1597248400434-7n83k6cpei4-f32b2c9a3ec6af4c375061698856ab9d-2flocked-on-sabres-bg.jpg?aid=rss_feed", ""),
//        PodcastEntity("","https://megaphone.imgix.net/podcasts/8e5bcebc-ca16-11ee-89f0-0fa0b9bdfc7c/image/11f568857987283428d892402e623b21.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress", ""),
//        PodcastEntity("","https://twoheadednerd.com/wp-content/uploads/2024/06/cropped-THN-New-Logo-1-32x32.png", ""),
//        PodcastEntity("","https://images.castfire.com/image/647/0/0/0/0-7935483.jpg", ""),
//        PodcastEntity("","https://megaphone.imgix.net/podcasts/6b2eb828-8e0b-11ea-b1dd-7f0b90d2a3ba/image/ff880f4262671ea9f60de03a153b607c.png?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress", "")
//    )
//    PodcastAppTheme {
//        HomeScreenUI(
//            subscriptions = subscriptions,
//            navigateToPodcast = {}
//        )
//    }
}

@Preview
@Composable
private fun SubscriptionImagePreview() {
    SubscriptionImage(
        image = "https://image.simplecastcdn.com/images/4638a61f-6fe2-4fc0-9398-c6118a63c1d6/65ba0b3e-50c6-4d13-aa12-40a6873e255b/3000x3000/microsoftteams-image-5.png?aid=rss_feed",
        navigateToPodcast = {}
    )
}