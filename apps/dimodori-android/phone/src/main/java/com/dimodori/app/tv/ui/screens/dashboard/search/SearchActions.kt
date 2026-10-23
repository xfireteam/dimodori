package com.dimodori.app.tv.ui.screens.dashboard.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dimodori.app.tv.ui.screens.detail.trackSelectorActivation
import com.jellycine.shared.R

/** Both controls share geometry and the existing filled TV-button focus treatment. */
@Composable
internal fun SearchActions(
    searchFocusRequester: FocusRequester,
    voiceFocusRequester: FocusRequester,
    onSearch: () -> Unit,
    onVoice: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchActionButton(
            icon = Icons.Default.Search,
            description = stringResource(R.string.search),
            onClick = onSearch,
            onDown = onDown,
            modifier = Modifier.focusRequester(searchFocusRequester)
                .focusProperties { right = voiceFocusRequester },
        )
        SearchActionButton(
            icon = Icons.Default.Mic,
            description = stringResource(R.string.tv_voice_search),
            onClick = onVoice,
            onDown = onDown,
            modifier = Modifier.focusRequester(voiceFocusRequester)
                .focusProperties { left = searchFocusRequester },
        )
    }
}

@Composable
private fun SearchActionButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .size(48.dp)
            .onFocusChanged { isFocused = it.isFocused }
            // Same full-press ownership as TV selectors: open after release,
            // consume repeats and both phases before clickable sees the event.
            .trackSelectorActivation(onClick)
            .onPreviewKeyEvent { event ->
                if (event.key == Key.DirectionDown) {
                    if (event.type == KeyEventType.KeyDown &&
                        event.nativeKeyEvent.repeatCount == 0
                    ) onDown()
                    true
                } else false
            }
            .clickable(onClick = onClick),
        color = if (isFocused) Color.White else Color.White.copy(alpha = 0.15f),
        shape = CircleShape,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (isFocused) Color.Black else Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
