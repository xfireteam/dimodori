package com.dimodori.app.tv.ui.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/** Consume both phases before the clickable's default keyboard handling. */
@Composable
internal fun Modifier.trackSelectorActivation(onActivate: () -> Unit): Modifier {
    val activation = remember { TrackSelectorActivation() }
    val currentAction by rememberUpdatedState(onActivate)
    return this
        .onFocusChanged { if (!it.isFocused) activation.reset() }
        .onPreviewKeyEvent { event ->
            when (event.key) {
                Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                    when (event.type) {
                        KeyEventType.KeyDown -> activation.keyDown(
                            event.nativeKeyEvent.keyCode,
                            event.nativeKeyEvent.repeatCount,
                        )
                        KeyEventType.KeyUp -> {
                            if (activation.keyUp(
                                    event.nativeKeyEvent.keyCode,
                                    event.nativeKeyEvent.isCanceled,
                                )) {
                                currentAction()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
}
