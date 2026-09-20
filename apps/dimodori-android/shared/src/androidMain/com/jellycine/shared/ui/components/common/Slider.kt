package com.jellycine.shared.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    inactiveTrackColor: Color = accentColor.copy(alpha = 0.24f),
    enableVerticalDpadNavigation: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val colors = SliderDefaults.colors(
        thumbColor = accentColor,
        activeTrackColor = accentColor,
        inactiveTrackColor = inactiveTrackColor,
        activeTickColor = accentColor.copy(alpha = 0.5f),
        inactiveTickColor = accentColor.copy(alpha = 0.5f)
    )
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onPreviewKeyEvent { event ->
            if (!enableVerticalDpadNavigation || event.type != KeyEventType.KeyDown) {
                return@onPreviewKeyEvent false
            }

            when (event.key) {
                Key.DirectionUp -> {
                    focusManager.moveFocus(FocusDirection.Up)
                    true
                }
                Key.DirectionDown -> {
                    focusManager.moveFocus(FocusDirection.Down)
                    true
                }
                else -> false
            }
        },
        valueRange = valueRange,
        steps = steps,
        colors = colors,
        thumb = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(accentColor, CircleShape)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = colors,
                modifier = Modifier.height(4.dp),
                thumbTrackGapSize = 0.dp,
                drawStopIndicator = null
            )
        }
    )
}