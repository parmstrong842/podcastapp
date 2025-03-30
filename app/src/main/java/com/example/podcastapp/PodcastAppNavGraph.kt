package com.example.podcastapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.podcastapp.audiocontroller.AudioControllerManager
import com.example.podcastapp.ui.screens.EpisodeScreen
import com.example.podcastapp.ui.screens.ExploreScreen
import com.example.podcastapp.ui.screens.HomeScreen
import com.example.podcastapp.ui.screens.PodcastPlayer
import com.example.podcastapp.ui.screens.PodcastScreen
import com.example.podcastapp.ui.screens.SearchScreen
import com.example.podcastapp.ui.screens.UserScreen
import com.example.podcastapp.ui.viewmodel.PodcastEpItem


private const val tag = "PodcastAppNavGraph"

sealed class NavigationScreen(val name: String, val icon: ImageVector?) {
    data object Home : NavigationScreen("Home", Icons.Default.Home)
    data object Explore : NavigationScreen("Explore", Icons.Default.Search)
    data object User : NavigationScreen("User", Icons.AutoMirrored.Filled.List)
    data object Search : NavigationScreen("Search", null)
    data object Podcast : NavigationScreen("Podcast", null)
    data object Episode : NavigationScreen("Episode", null)
}

val BottomNavigation = listOf(
    NavigationScreen.Home,
    NavigationScreen.Explore,
    NavigationScreen.User,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PodcastNavGraph(audioControllerManager: AudioControllerManager) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar{
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                BottomNavigation.forEach {
                    NavigationBarItem(
                        icon = { Icon(it.icon!!, contentDescription = it.name) },
                        label = { Text(it.name) },
                        selected = currentDestination?.route == it.name,
                        onClick = {
                            navController.navigate(it.name) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
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
            NavHost(navController, startDestination = NavigationScreen.Home.name) {
                composable(
                    route = NavigationScreen.Home.name
                ) {
                    HomeScreen(
                        navigateToPodcast = { navController.navigate("${NavigationScreen.Podcast.name}/$it") }
                    )
                }
                composable(NavigationScreen.Explore.name) {
                    ExploreScreen(
                        navigateToSearch = { navController.navigate(NavigationScreen.Search.name) },
                        playMedia = { audioControllerManager.playMedia(it) },
                        navigateToEpisode = { navController.navigate("${NavigationScreen.Episode.name}/$it") }
                    )
                }
                composable(NavigationScreen.User.name) {
                    UserScreen()
                }
                composable(
                    route = "${NavigationScreen.Podcast.name}/{podcastId}",
                    arguments = listOf(navArgument("podcastId") {
                        type = NavType.IntType
                    })
                ) {
                    PodcastScreen(
                        navigateBack = { navController.popBackStack() },
                        playMedia = { audioControllerManager.playMedia(it) },
                        navigateToEpisode = { navController.navigate("${NavigationScreen.Episode.name}/$it") }
                    )
                }
                composable(
                    route = "${NavigationScreen.Episode.name}/{episodeId}",
                    arguments = listOf(navArgument("episodeId") {
                        type = NavType.LongType
                    })
                ) {
                    EpisodeScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavigationScreen.Search.name) {
                    SearchScreen(
                        navigateBack = { navController.popBackStack() },
                        navigateToPodcast = {} // TODO:
                    )
                }
            }
//            Button(onClick = { audioControllerManager.playMedia(
//                PodcastEpItem(
//                    "https://megaphone.imgix.net/podcasts/6b48647a-f635-11ef-8324-2b180a017350/image/d142ec926f025bd64b32d9a2e96aa81a.jpg?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
//                    "The Joe Rogan Experience",
//                    "date",
//                    "#2282 - Bill Murray",
//                    "description",
//                    "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
//                    "1000",
//                    1,
//                    1
//                ),
//            ) },
//            ) {
//                Text("test")
//            }
            AnimatedVisibility(
                visible = audioControllerManager.hasPlaylistItems,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                PodcastPlayer(audioControllerManager = audioControllerManager)
            }
        }
    }
}