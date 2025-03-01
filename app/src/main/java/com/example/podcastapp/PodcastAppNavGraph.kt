package com.example.podcastapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.audiocontroller.AudioControllerManager
import com.example.podcastapp.ui.screens.ExploreScreen
import com.example.podcastapp.ui.screens.HomeScreen
import com.example.podcastapp.ui.screens.PodcastScreen
import com.example.podcastapp.ui.screens.SearchScreen
import com.example.podcastapp.ui.screens.UserScreen
import com.example.podcastapp.ui.theme.PodcastAppTheme


sealed class NavigationScreen(val name: String, val icon: ImageVector?) {
    data object Home : NavigationScreen("Home", Icons.Default.Home)
    data object Explore : NavigationScreen("Explore", Icons.Default.Search)
    data object User : NavigationScreen("User", Icons.AutoMirrored.Filled.List)
    data object Search : NavigationScreen("Search", null)
    data object Podcast : NavigationScreen("Podcast", null)
}

val BottomNavigation = listOf(
    NavigationScreen.Home,
    NavigationScreen.Explore,
    NavigationScreen.User,
)



@Composable
fun PodcastNavGraph() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
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
                        navigateToSearch = { navController.navigate(NavigationScreen.Search.name) }
                    )
                }
                composable(NavigationScreen.Search.name) {
                    SearchScreen(
                        navigateBack = { navController.popBackStack() },
                        navigateToPodcast = {} // TODO:
                    )
                }
                composable(
                    route = "${NavigationScreen.Podcast.name}/{podcastId}",
                    arguments = listOf(navArgument("podcastId") {
                        type = NavType.IntType
                    })
                ) {
                    PodcastScreen(
                        navigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavigationScreen.User.name) {
                    UserScreen()
                }
            }
            AnimatedVisibility(
                visible = AudioControllerManager.isPlaying,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                PodcastBottomBar()
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PodcastBottomBar(modifier: Modifier = Modifier) {
    val mediaInfo = AudioControllerManager.getCurrentMediaInfo()
    Row(
        modifier = modifier.fillMaxWidth().background(Color.Black)
    ) {
        GlideImage(
            model = mediaInfo?.imageUri,
            contentDescription = "episode image",
            modifier = Modifier
                .padding(end = 8.dp)
                .size(50.dp)
                .clip(RoundedCornerShape(6.dp)),
            loading = placeholder(R.drawable.ic_launcher_background),
            failure = placeholder(R.drawable.ic_launcher_foreground)
        )
        Column {
            Text(text = mediaInfo?.episodeName ?: "error")
            Text(text = mediaInfo?.title ?: "error")
        }
        IconButton(onClick = {
            AudioControllerManager.pauseMedia()
        }) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "play")
        }
    }
}

@Preview
@Composable
private fun PodcastBottomBarPreview() {
    PodcastAppTheme {
        PodcastBottomBar()
    }
}
