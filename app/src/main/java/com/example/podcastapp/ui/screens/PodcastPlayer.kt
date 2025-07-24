package com.example.podcastapp.ui.screens

import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.podcastapp.R
import com.example.podcastapp.audiocontroller.AudioControllerManagerMock
import com.example.podcastapp.audiocontroller.IAudioControllerManager
import com.example.podcastapp.ui.components.MediaSeekSlider
import com.example.podcastapp.ui.theme.Dimens
import com.example.podcastapp.ui.theme.PodcastAppTheme
import com.example.podcastapp.utils.formatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val tag = "PodcastPlayer"

enum class PlayerState{
    Collapsed,
    Expanded
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun PodcastPlayer(
    modifier: Modifier = Modifier,
    audioControllerManager: IAudioControllerManager,
    initialPlayerState: PlayerState = PlayerState.Collapsed,
    navigateToPodcast: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val adjustedHeight = with(density) {(configuration.screenHeightDp.dp - Dimens.playerCollapsedHeight - Dimens.navigationBarHeight).toPx()}

    val anchors = DraggableAnchors {
        PlayerState.Expanded at 0f
        PlayerState.Collapsed at adjustedHeight
    }

    val state = remember {
        AnchoredDraggableState(
            initialValue = initialPlayerState,
            anchors = anchors,
            positionalThreshold = { totalDistance: Float ->  totalDistance * 0.5f},
            velocityThreshold = { with(density) { 80.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay()
        )
    }

    val anchorFaction = (state.requireOffset() / adjustedHeight)

    val imageSizeMin = 50.dp
    val imageSizeDelta = configuration.screenHeightDp.dp / 3
    val imageSizeMax = imageSizeMin + imageSizeDelta
    val imageStartPadding = 8.dp
    val imageYExpanded = 50.dp
    val imageXExpanded = (configuration.screenWidthDp.dp - imageSizeMax) / 2 - imageStartPadding
    val imageRadius = 6.dp

    val collapsedAlpha by animateFloatAsState(
        targetValue = ((anchorFaction - 0.75f) * 4).coerceIn(0f, 1f),
        animationSpec = tween(0),
        label = "fadeAlpha"
    )

    val expandedAlpha by animateFloatAsState(
        targetValue = ((1f - anchorFaction - 0.75f) * 4).coerceIn(0f, 1f),
        animationSpec = tween(0),
        label = "fadeAlpha"
    )

    val imageSize by animateDpAsState(
        targetValue = (imageSizeMin + imageSizeDelta * (1f - anchorFaction)),
        animationSpec = tween(0),
        label = "imageSize"
    )

    val imageYTranslation by animateFloatAsState(
        targetValue = (with(density) {imageYExpanded.toPx()} * (1f - anchorFaction)),
        animationSpec = tween(0),
        label = "imageY"
    )

    val imageXTranslation by animateFloatAsState(
        targetValue = (with(density) {imageXExpanded.toPx()} * (1f - anchorFaction)),
        animationSpec = tween(0),
        label = "imageY"
    )

    val imageCornerRadius by animateDpAsState(
        targetValue = (with(density) {imageRadius.toPx()} * (1f - anchorFaction)).dp,
        animationSpec = tween(0),
        label = "imageY"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = state.requireOffset()
            }
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Vertical,
            ),
    ){
        var progress by remember { mutableFloatStateOf(0f) }
        var bufferProgress by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(Unit) {
            while (true) {
                progress = audioControllerManager.getProgress()
                bufferProgress = audioControllerManager.getBufferProgress()
                delay(2000)
            }
        }

        Box {
            CollapsedPlayer(
                isCollapsed = state.currentValue == PlayerState.Collapsed,
                rowAlpha = collapsedAlpha,
                currentProgress = progress,
                audioControllerManager = audioControllerManager,
                expandPlayer = {
                    scope.launch {
                        state.animateTo(PlayerState.Expanded)
                    }
                }
            )
            ExpandedPlayer(
                topPadding = imageSizeMax + imageYExpanded + 20.dp,
                expandedAlpha = expandedAlpha,
                currentProgress = progress,
                currentBufferProgress = bufferProgress,
                audioControllerManager = audioControllerManager,
                navigateToPodcast = {
                    scope.launch {
                        state.animateTo(PlayerState.Collapsed)
                    }
                    navigateToPodcast(it)
                }
            )
            IconButton(
                onClick = {
                    scope.launch {
                        state.animateTo(PlayerState.Collapsed)
                    }
                },
                enabled = state.currentValue == PlayerState.Expanded,
                modifier = Modifier.alpha(expandedAlpha)
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "collapse player")
            }
            GlideImage(
                model = audioControllerManager.currentMediaInfo?.imageUri,
                contentDescription = "episode image",
                modifier = Modifier
                    .padding(start = imageStartPadding, top = 4.dp)
                    .size(imageSize)
                    .graphicsLayer {
                        translationY = imageYTranslation
                        translationX = imageXTranslation
                    }
                    .clip(RoundedCornerShape(imageCornerRadius)),
                loading = placeholder(R.drawable.ic_launcher_background),
                failure = placeholder(R.drawable.ic_launcher_foreground),
                requestBuilderTransform = {
                    it.override(300)
                }
            )
        }
    }
}

@Composable
private fun ExpandedPlayer(
    topPadding: Dp,
    expandedAlpha: Float,
    currentProgress: Float,
    currentBufferProgress: Float,
    audioControllerManager: IAudioControllerManager,
    navigateToPodcast: (Int) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val mediaInfo = audioControllerManager.currentMediaInfo

    val sidePadding = 30.dp
    Column(
        modifier = Modifier
            .alpha(expandedAlpha)
            .padding(start = sidePadding, top = topPadding, end = sidePadding),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = mediaInfo?.episodeName ?: "error",
                modifier = Modifier.basicMarquee(),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = mediaInfo?.title ?: "error",
                modifier = Modifier
                    .alpha(0.75f)
                    .clickable {
                        audioControllerManager.getCurrentPodcastId()?.let {
                            navigateToPodcast(it)
                        }
                    },
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Save")
            }
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Download")
            }
        }
        PlaybackProgress(currentProgress, currentBufferProgress, audioControllerManager)
        PlaybackControlButtonRow(
            audioControllerManager = audioControllerManager,
            onClickSleepTimer = { showBottomSheet = true }
        )
        SleepTimerBottomSheet(
            showBottomSheet,
            hideBottomSheet = {
                showBottomSheet = false
            },
            audioControllerManager
        )
    }
}

@Composable
private fun PlaybackControlButtonRow(
    modifier: Modifier = Modifier,
    audioControllerManager: IAudioControllerManager,
    onClickSleepTimer: () -> Unit
) {
    val shouldShowPlayButton = audioControllerManager.shouldShowPlayButton
    val currentSpeed by audioControllerManager.currentSpeed.collectAsState()
    val getSpeedIcon = {
        when (currentSpeed) {
            "0.5x" -> R.drawable.speed_0_5x_24
            "0.7x" -> R.drawable.speed_0_7x_24
            "1x" -> R.drawable.speed_1x_24
            "1.2x" -> R.drawable.speed_1_2x_24
            "1.5x" -> R.drawable.speed_1_5x_24
            "1.7x" -> R.drawable.speed_1_7x_24
            "2x" -> R.drawable.speed_2x_24
            else -> R.drawable.speed_1x_24
        }
    }

    val handleSpeedChange = {
        when (currentSpeed) {
            "0.5x" -> audioControllerManager.sendCustomCommand("SPEED_0_5X", Bundle())
            "0.7x" -> audioControllerManager.sendCustomCommand("SPEED_0_7X", Bundle())
            "1x" -> audioControllerManager.sendCustomCommand("SPEED_1X", Bundle())
            "1.2x" -> audioControllerManager.sendCustomCommand("SPEED_1_2X", Bundle())
            "1.5x" -> audioControllerManager.sendCustomCommand("SPEED_1_5X", Bundle())
            "1.7x" -> audioControllerManager.sendCustomCommand("SPEED_1_7X", Bundle())
            "2x" -> audioControllerManager.sendCustomCommand("SPEED_2X", Bundle())
            else -> audioControllerManager.sendCustomCommand("SPEED_1X", Bundle())
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val playIconSize = 52.dp
        val seekIconSize = 32.dp
        IconButton(onClick = { handleSpeedChange() }) {
            Icon(painterResource(getSpeedIcon()), contentDescription = "change playback speed")
        }
        IconButton(
            onClick = { audioControllerManager.sendCustomCommand("SEEK_BACK", Bundle()) },
        ) {
            Icon(
                painterResource(id = R.drawable.baseline_replay_10_24),
                contentDescription = "back 10",
                modifier = Modifier.size(seekIconSize)
            )
        }
        IconButton(
            onClick = {
                if (shouldShowPlayButton) {
                    audioControllerManager.resumePlayback()
                } else {
                    audioControllerManager.pauseMedia()
                }
            },
            modifier = Modifier.size(playIconSize)
        ) {
            if (audioControllerManager.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (shouldShowPlayButton) {
                        Icons.Filled.PlayArrow
                    } else {
                        Icons.Filled.Pause
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = if (shouldShowPlayButton) "Play" else "Pause",
                )
            }
        }
        IconButton(
            onClick = { audioControllerManager.sendCustomCommand("SEEK_FORWARD", Bundle()) },
        ) {
            Icon(
                painterResource(id = R.drawable.baseline_forward_30_24),
                contentDescription = "forward 30",
                modifier = Modifier.size(seekIconSize)
            )
        }
        IconButton(onClick = onClickSleepTimer) {
            Icon(
                painterResource(
                    id = if (audioControllerManager.sleepTimerActive) R.drawable.bedtime_filled_24dp else R.drawable.bedtime_24dp
                ),
                contentDescription = "sleep timer"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerBottomSheet(
    showBottomSheet: Boolean,
    hideBottomSheet: () -> Unit,
    audioControllerManager: IAudioControllerManager
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = hideBottomSheet,
            sheetState = sheetState,
            dragHandle = { Text("Sleep timer", modifier = Modifier.padding(top = 10.dp)) }
        ) {
            Column(modifier = Modifier.padding(start = 40.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            scope
                                .launch {
                                    sheetState.hide()
                                    audioControllerManager.cancelSleepTimer()
                                }
                                .invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        hideBottomSheet()
                                    }
                                }
                        }
                ) {
                    Text(text = "No timer")
                }
                SleepTimerOption(
                    label = "3 seconds",
                    durationMillis = 3 * 1000L,
                    sheetState = sheetState,
                    scope = scope,
                    hideBottomSheet = hideBottomSheet,
                    audioControllerManager
                )
                SleepTimerOption(
                    label = "5 minutes",
                    durationMillis = 5 * 60 * 1000L,
                    sheetState = sheetState,
                    scope = scope,
                    hideBottomSheet = hideBottomSheet,
                    audioControllerManager
                )
                SleepTimerOption(
                    label = "15 minutes",
                    durationMillis = 15 * 60 * 1000L,
                    sheetState = sheetState,
                    scope = scope,
                    hideBottomSheet = hideBottomSheet,
                    audioControllerManager
                )
                SleepTimerOption(
                    label = "30 minutes",
                    durationMillis = 30 * 60 * 1000L,
                    sheetState = sheetState,
                    scope = scope,
                    hideBottomSheet = hideBottomSheet,
                    audioControllerManager
                )
                SleepTimerOption(
                    label = "1 hour",
                    durationMillis = 60 * 60 * 1000L,
                    sheetState = sheetState,
                    scope = scope,
                    hideBottomSheet = hideBottomSheet,
                    audioControllerManager
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerOption(
    label: String,
    durationMillis: Long,
    sheetState: SheetState,
    scope: CoroutineScope,
    hideBottomSheet: () -> Unit,
    audioControllerManager: IAudioControllerManager
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable {
                scope
                    .launch {
                        sheetState.hide()
                        audioControllerManager.sleepTimer(durationMillis)
                    }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            hideBottomSheet()
                        }
                    }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PlaybackProgress(
    currentProgress: Float,
    currentBufferProgress: Float,
    audioControllerManager: IAudioControllerManager
) {
    var currentTime by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = audioControllerManager.getMediaController()?.currentPosition ?: 0L
            totalDuration = audioControllerManager.getMediaController()?.duration ?: 0L
            delay(100L)
        }
    }

    Column {
        MediaSeekSlider(currentProgress, currentBufferProgress) {
            audioControllerManager.seekToProgress(it)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentTime),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = formatTime(totalDuration),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun CollapsedPlayer(
    isCollapsed: Boolean,
    rowAlpha: Float,
    currentProgress: Float,
    audioControllerManager: IAudioControllerManager,
    expandPlayer: () -> Unit,
) {
    val mediaInfo = audioControllerManager.currentMediaInfo
    val shouldShowPlayButton = audioControllerManager.shouldShowPlayButton

    Column(
        modifier = Modifier.alpha(rowAlpha)
    ) {
        Row(
            Modifier
                .height(58.dp)
                .padding(start = 8.dp, end = 8.dp)
                .clickable(
                    enabled = isCollapsed
                ) {
                    expandPlayer()
                }
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 62.dp)
            ) {
                Text(
                    text = mediaInfo?.episodeName ?: "error",
                    modifier = Modifier.basicMarquee(),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = mediaInfo?.title ?: "error",
                    modifier = Modifier.alpha(0.65f),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            IconButton(
                onClick = {
                    if (shouldShowPlayButton) {
                        audioControllerManager.resumePlayback()
                    } else {
                        audioControllerManager.pauseMedia()
                    }
                },
                enabled = isCollapsed
            ) {
                if (audioControllerManager.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (shouldShowPlayButton) {
                            Icons.Filled.PlayArrow
                        } else {
                            Icons.Filled.Pause
                        },
                        contentDescription = if (shouldShowPlayButton) "Play" else "Pause",
                    )
                }
            }
        }
        ProgressIndicator(
            progress = { currentProgress },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    progressColor: Color = ProgressIndicatorDefaults.linearColor,
) {
    val coercedProgress = { progress().coerceIn(0f, 1f) }
    Canvas(
        modifier.size(240.dp, 2.dp)
    ) {
        val strokeWidth = size.height
        val currentCoercedProgress = coercedProgress()

        drawLinearIndicator(trackColor, strokeWidth, 0f, 1f)
        drawLinearIndicator(progressColor, strokeWidth, 0f, currentCoercedProgress)
    }
}

private fun DrawScope.drawLinearIndicator(
    color: Color,
    strokeWidth: Float,
    startFraction: Float,
    endFraction: Float,
    thumbWidth: Dp = 0.dp
) {
    val width = size.width
    val height = size.height
    // Start drawing from the vertical center of the stroke
    val yOffset = height / 2

    val barStart = startFraction * width
    val barEnd = endFraction * width

    drawLine(
        color,
        Offset(barStart + thumbWidth.toPx(), yOffset),
        Offset(barEnd - thumbWidth.toPx(), yOffset),
        strokeWidth
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PodcastPlayer_Collapsed_Preview() {
    PodcastAppTheme() {
        PodcastPlayer(audioControllerManager = AudioControllerManagerMock()) { }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun PodcastPlayer_Expanded_Preview() {
    PodcastAppTheme {
        PodcastPlayer(Modifier, AudioControllerManagerMock(), PlayerState.Expanded) { }
    }
}

@Preview
@Composable
private fun PlaybackControlButtonRow_Paused_Preview() {
    PlaybackControlButtonRow(
        audioControllerManager = AudioControllerManagerMock(),
        onClickSleepTimer = {}
    )
}

@Preview
@Composable
private fun PlaybackControlButtonRow_Playing_Preview() {
    PlaybackControlButtonRow(
        audioControllerManager = AudioControllerManagerMock(shouldShowPlayButton = false),
        onClickSleepTimer = {}
    )
}

@Preview
@Composable
private fun PlaybackControlButtonRow_Loading_Preview() {
    PlaybackControlButtonRow(
        audioControllerManager = AudioControllerManagerMock(isLoading = true),
        onClickSleepTimer = {}
    )
}

@Preview
@Composable
private fun PlaybackControlButtonRow_SleepTimerActive_Preview() {
    PlaybackControlButtonRow(
        audioControllerManager = AudioControllerManagerMock(sleepTimerActive = true),
        onClickSleepTimer = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SleepTimerOption_Preview() {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    SleepTimerOption(
        label = "5 minutes",
        durationMillis = 5 * 60 * 1000L,
        sheetState = sheetState,
        scope = scope,
        hideBottomSheet = {},
        AudioControllerManagerMock()
    )
}