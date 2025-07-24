package com.example.podcastapp.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import com.example.podcastapp.ui.theme.PodcastAppTheme
import com.example.podcastapp.ui.theme.activeTrackDark
import com.example.podcastapp.ui.theme.activeTrackLight
import com.example.podcastapp.ui.theme.bufferTrackDark
import com.example.podcastapp.ui.theme.bufferTrackLight
import com.example.podcastapp.ui.theme.inactiveTrackDark
import com.example.podcastapp.ui.theme.inactiveTrackLight
import kotlinx.coroutines.coroutineScope
import kotlin.math.max
import kotlin.math.roundToInt


private const val tag = "MediaSeekSlider"

@Composable
fun MediaSeekSlider(
    currentProgress: Float,
    currentBufferProgress: Float,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChangeFinish: (Float) -> Unit,
) {
    var sliderPosition by remember { mutableFloatStateOf(currentProgress) }
    var isUserSeeking by remember { mutableStateOf(false) }

    val interactions = remember { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    val state =
        remember { SliderState(
            value = currentProgress,
            bufferValue = currentBufferProgress,
            onValueChange = {
                sliderPosition = it
                isUserSeeking = true
            },
            onValueChangeFinished = {
                onValueChangeFinish(sliderPosition)
                isUserSeeking = false
            }
        ) }

    LaunchedEffect(currentProgress) {
        if (!isUserSeeking) {
            sliderPosition = currentProgress
            state.value = currentProgress
        }
    }

    state.bufferValue = currentBufferProgress

    val press = Modifier.sliderTapModifier(state, interactionSource)
    val drag =
        Modifier.draggable(
            orientation = Orientation.Horizontal,
            interactionSource = interactionSource,
            onDragStopped = { state.gestureEndAction() },
            startDragImmediately = state.isDragging,
            state = state
        )

    Layout(
        {
            Box(
                modifier =
                    Modifier
                        .layoutId(SliderComponents.THUMB)
                        .wrapContentWidth()
            ) {
                Thumb(interactionSource, interactions)
            }
            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) {
                Track(state)
            }
        },
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .focusable(interactionSource = interactionSource)
                .then(press)
                .then(drag)
    ) { measurables, constraints ->
        //Log.d(tag, "measure layout") // TODO:
        val thumbPlaceable =
            measurables.fastFirst { it.layoutId == SliderComponents.THUMB }.measure(constraints)

        val isPressed = interactions.isNotEmpty()
        val bigThumb = if (isPressed) thumbPlaceable.width else thumbPlaceable.width * 2

        val trackPlaceable =
            measurables
                .fastFirst { it.layoutId == SliderComponents.TRACK }
                .measure(constraints.offset(horizontal = -bigThumb).copy(minHeight = 0))

        val sliderWidth = bigThumb + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        state.updateDimensions(sliderWidth)

        val smallThumbOffsetX = if (isPressed) 0 else thumbPlaceable.width / 2
        val trackOffsetX = bigThumb / 2
        //Log.d(tag, "state.value: ${state.value}") // TODO:
        val thumbOffsetX = (trackPlaceable.width * state.value).roundToInt() + smallThumbOffsetX
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(sliderWidth, sliderHeight) {
            trackPlaceable.placeRelative(trackOffsetX, trackOffsetY)
            thumbPlaceable.placeRelative(thumbOffsetX, thumbOffsetY)
        }
    }
}

@Stable
private fun Modifier.sliderTapModifier(
    state: SliderState,
    interactionSource: MutableInteractionSource,
) = pointerInput(state, interactionSource) {
        detectTapGestures(
            onPress = { state.onPress(it) },
            onTap = {
                state.dispatchRawDelta(0f)
                state.gestureEndAction()
            }
        )
    }

class SliderState(
    value: Float = 0f,
    bufferValue: Float = 0f,
    val onValueChange: (Float) -> Unit,
    val onValueChangeFinished: (() -> Unit),
) : DraggableState {

    private var valueState by mutableFloatStateOf(value)
    private var bufferState by mutableFloatStateOf(bufferValue)

    var value: Float
        set(newVal) {
            valueState = newVal
        }
        get() = valueState

    var bufferValue: Float
        set(newVal) {
            bufferState = newVal
        }
        get() = bufferState

    internal var isDragging by mutableStateOf(false)
    private val scrollMutex = MutatorMutex()
    private var totalWidth by mutableIntStateOf(0)

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit
    ): Unit = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }

    internal fun onPress(pos: Offset) {
        pressOffset = pos.x - rawOffset
    }

    private var rawOffset by mutableFloatStateOf(0f)
    private var pressOffset by mutableFloatStateOf(0f)
    private val dragScope: DragScope =
        object : DragScope {
            override fun dragBy(pixels: Float): Unit = dispatchRawDelta(pixels)
        }

    override fun dispatchRawDelta(delta: Float) {
        //Log.d(tag, "delta: $delta") // TODO:
        //Log.d(tag, "rawOffset: $rawOffset") // TODO:
        rawOffset = (rawOffset + delta + pressOffset)
            .coerceIn(0f, totalWidth.toFloat())
        pressOffset = 0f
        val fraction = rawOffset / totalWidth
        value = fraction
        //Log.d(tag, "fraction: $fraction") // TODO:
        onValueChange(fraction)
    }

    internal val gestureEndAction = {
        if (!isDragging) {
            // check isDragging in case the change is still in progress (touch -> drag case)
            onValueChangeFinished.invoke()
        }
    }

    internal fun updateDimensions(newTotalWidth: Int) {
        totalWidth = newTotalWidth
        //Log.d(tag, "totalWidth: $totalWidth") // TODO:
    }
}

@Composable
private fun Track(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    inactiveLight: Color = inactiveTrackLight,
    bufferLight:   Color = bufferTrackLight,
    activeLight:   Color = activeTrackLight,
    inactiveDark:  Color = inactiveTrackDark,
    bufferDark:    Color = bufferTrackDark,
    activeDark:    Color = activeTrackDark,
    height: Dp = 4.dp
) {

    val isDark = isSystemInDarkTheme()
    val inactiveColor = if (isDark) inactiveDark else inactiveLight
    val bufferColor   = if (isDark) bufferDark   else bufferLight
    val activeColor   = if (isDark) activeDark   else activeLight

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        //inactive track
        drawTrackPath(
            Offset.Zero,
            Offset(size.width, size.height),
            inactiveColor,
        )

        //buffer track
        drawTrackPath(
            Offset.Zero,
            Offset(sliderState.bufferValue * size.width, size.height),
            bufferColor,
        )

        //active track
        drawTrackPath(
            Offset.Zero,
            Offset(sliderState.value * size.width, size.height),
            activeColor,
        )
    }
}

private fun DrawScope.drawTrackPath(
    topLeft: Offset,
    bottomRight: Offset,
    color: Color,
) {
    val path = Path().apply {
        addRect(Rect(topLeft, bottomRight))
    }
    drawPath(path, color)
}

@Composable
private fun Thumb(
    interactionSource: MutableInteractionSource,
    interactions: SnapshotStateList<Interaction>,
    modifier: Modifier = Modifier,
    lightColor: Color = Color.Black,
    darkColor: Color = Color.White,
    shape: Shape = CircleShape,
    thumbSize: DpSize = DpSize(12.dp, 12.dp)
) {

    val size =
        if (interactions.isNotEmpty()) {
            thumbSize.copy(width = thumbSize.width * 2, height = thumbSize.height * 2)
        } else {
            thumbSize
        }
    Spacer(
        modifier
            .size(size)
            .hoverable(interactionSource = interactionSource)
            .background(if (isSystemInDarkTheme()) darkColor else lightColor, shape)
    )
}

private enum class SliderComponents {
    THUMB,
    TRACK
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = false, showSystemUi = false
)
@Preview(showBackground = true)
@Composable
private fun MediaSeekSlider_Preview() {
    PodcastAppTheme {
        Column {
            MediaSeekSlider(
                currentProgress = 0.25f,
                currentBufferProgress = 0.5f,
                onValueChangeFinish = {}
            )
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
            )
        }
    }
}