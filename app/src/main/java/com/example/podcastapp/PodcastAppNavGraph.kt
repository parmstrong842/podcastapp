package com.example.podcastapp

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.example.podcastapp.audiocontroller.IAudioController
import com.example.podcastapp.ui.screens.EpisodeScreen
import com.example.podcastapp.ui.screens.ExploreScreen
import com.example.podcastapp.ui.screens.HomeScreen
import com.example.podcastapp.ui.components.PlayerState
import com.example.podcastapp.ui.components.PodcastPlayer
import com.example.podcastapp.ui.screens.PodcastScreen
import com.example.podcastapp.ui.screens.SearchScreen
import com.example.podcastapp.ui.screens.UserScreen
import com.example.podcastapp.ui.theme.Dimens
import com.example.podcastapp.ui.viewmodel.EpisodeViewModel
import com.example.podcastapp.ui.viewmodel.PodcastViewModel
import kotlinx.coroutines.launch


private const val tag = "PodcastAppNavGraph"

sealed class NavigationScreen(val name: String, val icon: ImageVector?) {
    data object Home : NavigationScreen("Home", Icons.Default.Home)
    data object Explore : NavigationScreen("Explore", Icons.Default.Search)
    data object User : NavigationScreen("User", Icons.AutoMirrored.Filled.List)
    data object Search : NavigationScreen("Search", null)
    data class Podcast(val feedUrl: String) : NavigationScreen("Podcast", null)
    data class Episode(val guid: String) : NavigationScreen("Episode", null)
}

val BottomNavigation = listOf(
    NavigationScreen.Home,
    NavigationScreen.Explore,
    NavigationScreen.User,
)

@Composable
fun PodcastNavGraph(audioController: IAudioController) {

    val topLevelBackStack = remember { TopLevelBackStack<Any>(NavigationScreen.Home, BottomNavigation) }

    val scope = rememberCoroutineScope()
    val playerState = rememberAnchoredDraggableState()

    Scaffold(
        bottomBar = {
            NavigationBar{
                BottomNavigation.forEach { topLevelRoute ->
                    NavigationBarItem(
                        icon = { Icon(topLevelRoute.icon!!, contentDescription = topLevelRoute.name) },
                        label = { Text(topLevelRoute.name) },
                        selected = topLevelRoute == topLevelBackStack.topLevelKey,
                        onClick = {
                            topLevelBackStack.add(topLevelRoute)
                            scope.launch {
                                playerState.animateTo(PlayerState.Collapsed)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = topLevelBackStack.backStack,
                onBack = { topLevelBackStack.removeLast() },
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<NavigationScreen.Home> {
                        HomeScreen(
                            navigateToPodcast = { topLevelBackStack.add(NavigationScreen.Podcast(it)) },
                        )
                    }
                    entry<NavigationScreen.Explore> {
                        ExploreScreen(
                            navigateToSearch = { topLevelBackStack.add(NavigationScreen.Search) },
                            playMedia = { audioController.playMedia(it) },
                            navigateToEpisode = { topLevelBackStack.add(NavigationScreen.Episode(it)) }
                        )
                    }
                    entry<NavigationScreen.User> {
                        UserScreen(
                            playMedia = { audioController.playMedia(it) },
                            navigateToPodcast = { topLevelBackStack.add(NavigationScreen.Podcast(it)) },
                            navigateToEpisode = { topLevelBackStack.add(NavigationScreen.Episode(it)) }
                        )
                    }
                    entry<NavigationScreen.Podcast> {
                        PodcastScreen(
                            viewModel = viewModel(factory = PodcastViewModel.Factory(it.feedUrl, LocalContext.current.applicationContext as PodcastApplication)),
                            navigateBack = { topLevelBackStack.removeLast() },
                            playMedia = { podcastEpItem ->  audioController.playMedia(podcastEpItem) },
                            navigateToEpisode = { episodeID -> topLevelBackStack.add(NavigationScreen.Episode(episodeID)) }
                        )
                    }
                    entry<NavigationScreen.Episode> {
                        EpisodeScreen(
                            viewModel = viewModel(factory = EpisodeViewModel.Factory(it.guid, LocalContext.current.applicationContext as PodcastApplication)),
                            navigateBack = { topLevelBackStack.removeLast() },
                            audioController = audioController
                        )
                    }
                    entry<NavigationScreen.Search> {
                        SearchScreen(
                            navigateBack = { topLevelBackStack.removeLast() },
                            navigateToPodcast = { topLevelBackStack.add(NavigationScreen.Podcast(it)) }
                        )
                    }
                }
            )
//            Button(onClick = { audioController.playMedia(
//                PodcastEpItem(
//                    podcastTitle = "The Joe Rogan Experience",
//                    podcastImage = "https://megaphone.imgix.net/podcasts/6b48647a-f635-11ef-8324-2b180a017350/image/d142ec926f025bd64b32d9a2e96aa81a.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
//                    pubDate = "date",
//                    episodeTitle = "Kalimba",
//                    episodeImage = "https://megaphone.imgix.net/podcasts/6b48647a-f635-11ef-8324-2b180a017350/image/d142ec926f025bd64b32d9a2e96aa81a.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
//                    episodeDescription = "description",
//                    enclosureUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
//                    timeLeft = "30:00",
//                    progress = 0.5f,
//                    feedUrl = "feedUrl",
//                    guid = "1",
//                    finished = false
//                ),
//            ) },
//            ) {
//                Text("test")
//            }
            AnimatedVisibility(
                visible = audioController.hasPlaylistItems,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                PodcastPlayer(
                    state = playerState,
                    audioController = audioController,
                    navigateToPodcast = { topLevelBackStack.add(NavigationScreen.Podcast(it)) }
                )
            }
        }
    }
}

@Composable
private fun rememberAnchoredDraggableState(): AnchoredDraggableState<PlayerState> {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    return remember(configuration.screenHeightDp, density.density) {
        val adjustedHeight =
            with(density) { (configuration.screenHeightDp.dp - Dimens.playerCollapsedHeight - Dimens.navigationBarHeight).toPx() }

        val anchors = DraggableAnchors {
            PlayerState.Expanded at 0f
            PlayerState.Collapsed at adjustedHeight
        }

        AnchoredDraggableState(
            initialValue = PlayerState.Collapsed,
            anchors = anchors,
            positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f },
            velocityThreshold = { with(density) { 80.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay()
        )
    }
}

class TopLevelBackStack<T: Any>(startKey: T, private val topLevelKeys: List<Any>) {

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    fun add(key: T) {
        if (topLevelKeys.contains(key)) {
            backStack.clear()
            topLevelKey = key
        }
        backStack.add(key)
        log()
    }

    fun removeLast() {
        backStack.removeLastOrNull()
        log()
    }

    private fun log() {
        val stackDump = backStack.joinToString(" -> ")
        Log.d(tag, "backstack: $stackDump")
    }
}