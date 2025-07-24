package com.example.podcastapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import com.example.podcastapp.R

@Composable
fun PlayTimerButton(
    modifier: Modifier = Modifier,
    timeLeft: String,
    progress: Float,
    played: Boolean,
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
                played -> Text(stringResource(R.string.completed), color = Color.LightGray)
                progress > 0f -> {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(timeLeft)
                }
                else -> {
                    Icon(Icons.Default.PlayCircleOutline, contentDescription = "play")
                    Spacer(Modifier.width(4.dp))
                    Text(timeLeft)
                }
            }
        }
    }
}