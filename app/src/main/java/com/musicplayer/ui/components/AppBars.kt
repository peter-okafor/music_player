package com.musicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.theme.TextSecondary

/**
 * The single header used across every library screen.
 *
 * Collapsing the title into an inline search field (rather than pushing the
 * user to a separate search screen) keeps the list in place while they type.
 */
@Composable
fun LibraryHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    searchQuery: String? = null,
    onSearchQueryChange: ((String) -> Unit)? = null,
    searchActive: Boolean = false,
    onSearchActiveChange: ((Boolean) -> Unit)? = null,
    searchPlaceholder: String = "Search",
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchActive) {
        if (searchActive) {
            runCatching { focusRequester.requestFocus() }
        } else {
            focusManager.clearFocus()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (onNavigateBack != null) 4.dp else 20.dp, end = 8.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onNavigateBack != null) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

        TitleOrSearchField(
            modifier = Modifier.weight(1f),
            title = title,
            subtitle = subtitle,
            searchActive = searchActive,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            searchPlaceholder = searchPlaceholder,
            focusRequester = focusRequester,
            onSearchSubmit = { focusManager.clearFocus() }
        )

        if (onSearchActiveChange != null) {
            IconButton(
                onClick = {
                    if (searchActive) {
                        onSearchQueryChange?.invoke("")
                        onSearchActiveChange(false)
                    } else {
                        onSearchActiveChange(true)
                    }
                }
            ) {
                Icon(
                    imageVector = if (searchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                    contentDescription = if (searchActive) "Close search" else "Search",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        actions()
    }
}

@Composable
private fun TitleOrSearchField(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    searchActive: Boolean,
    searchQuery: String?,
    onSearchQueryChange: ((String) -> Unit)?,
    searchPlaceholder: String,
    focusRequester: FocusRequester,
    onSearchSubmit: () -> Unit
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !searchActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            HeaderTitle(title = title, subtitle = subtitle)
        }

        AnimatedVisibility(
            visible = searchActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TextField(
                value = searchQuery.orEmpty(),
                onValueChange = { onSearchQueryChange?.invoke(it) },
                placeholder = {
                    Text(
                        text = searchPlaceholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
    }
}

@Composable
private fun HeaderTitle(title: String, subtitle: String?) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
