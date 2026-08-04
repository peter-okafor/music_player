package com.musicplayer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.SortState
import com.musicplayer.data.model.TrackSort

/** Sort control for track lists: field picker plus an ascending toggle. */
@Composable
fun TrackSortMenu(
    state: SortState,
    onChange: (SortState) -> Unit,
    modifier: Modifier = Modifier,
    options: List<TrackSort> = listOf(
        TrackSort.TITLE,
        TrackSort.ARTIST,
        TrackSort.ALBUM,
        TrackSort.DURATION,
        TrackSort.DATE_ADDED
    )
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Rounded.SwapVert,
                contentDescription = "Sort",
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                val selected = option == state.trackSort
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onChange(state.copy(trackSort = option))
                    },
                    trailingIcon = {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text(if (state.descending) "Descending" else "Ascending") },
                onClick = {
                    expanded = false
                    onChange(state.copy(descending = !state.descending))
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (state.descending) {
                            Icons.Rounded.ArrowDownward
                        } else {
                            Icons.Rounded.ArrowUpward
                        },
                        contentDescription = null
                    )
                }
            )
        }
    }
}
