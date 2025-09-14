package com.example.podcastapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.podcastapp.R

@Composable
fun PlayTimerButton(
    modifier: Modifier = Modifier,
    timeLeft: String,
    progress: Float,
    finished: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                finished -> Text(stringResource(R.string.completed), color = Color.LightGray)
                progress > 0f -> {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(timeLeft)
                }
                else -> {
                    Icon(Icons.Default.PlayCircleOutline, contentDescription = "play")
                    Spacer(Modifier.width(4.dp))
                    Text(if (timeLeft == "00:00") "__:__" else timeLeft)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlayTimerButton_Preview() {
    PlayTimerButton(
        timeLeft = "00:30:00",
        progress = 0.5f,
        finished = false,
        onClick = {}
    )
}

@Preview
@Composable
private fun PlayTimerButton_Unplayed_Preview() {
    PlayTimerButton(
        timeLeft = "00:30:00",
        progress = 0.0f,
        finished = false,
        onClick = {}
    )
}