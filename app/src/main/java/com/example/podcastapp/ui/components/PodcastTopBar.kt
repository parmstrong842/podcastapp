package com.example.podcastapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        } else {
            Box(Modifier.size(24.dp))
        }
    }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun TopBarPreview() {
    PodcastTopBar(
        text = "PodcastApp",
        menuItems = listOf(android.R.string.copy, android.R.string.cut, android.R.string.paste)
    )
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun TopBarPreview_EmptyList() {
    PodcastTopBar(
        text = "PodcastApp",
        menuItems = emptyList()
    )
}