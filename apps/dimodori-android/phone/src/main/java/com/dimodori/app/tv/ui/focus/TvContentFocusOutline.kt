package com.dimodori.app.tv.ui.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

internal class TvContentFocus {
    var focused by mutableStateOf(false)
}

@Composable
internal fun rememberTvContentFocus(): TvContentFocus = remember { TvContentFocus() }

/** Observes the existing clickable's focus target; does not create a new one. */
internal fun Modifier.tvContentFocusObserver(focus: TvContentFocus): Modifier =
    onFocusChanged { focus.focused = it.hasFocus }

/**
 * Apply to the image container, not the caption or clickable row. Paint last,
 * entirely inside its bounds, so images/overlays and parent clipping cannot
 * hide the stroke. This modifier never measures, adds padding, or handles keys.
 */
internal fun Modifier.tvContentFocusOutline(
    type: String?,
    focus: TvContentFocus,
    shape: Shape,
): Modifier = drawTvContentFocusOutline(type, { focus.focused }, shape)

/** Opt-in adapter for shared cards, whose own observer supplies the focus. */
internal fun Modifier.tvContentFocusOutline(
    type: String?,
    focused: Boolean,
    shape: Shape,
): Modifier = drawTvContentFocusOutline(type, { focused }, shape)

private fun Modifier.drawTvContentFocusOutline(
    type: String?,
    focused: () -> Boolean,
    shape: Shape,
): Modifier = drawWithCache {
    val strokeWidth = 2.dp.toPx()
    val inset = strokeWidth / 2f
    val outline = shape.createOutline(
        Size((size.width - strokeWidth).coerceAtLeast(0f),
            (size.height - strokeWidth).coerceAtLeast(0f)),
        layoutDirection,
        this,
    )
    onDrawWithContent {
        drawContent()
        // Read focus in the draw phase: no image/layout recomposition required.
        if (size.width > strokeWidth && size.height > strokeWidth &&
            showsTvContentFocusOutline(type, focused())
        ) {
            translate(inset, inset) {
                drawOutline(outline, Color.White, style = Stroke(strokeWidth))
            }
        }
    }
}
