@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.dimodori.app.ui.screens.player

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.dimodori.app.ui.activity.DimodoriActivity
import com.dimodori.app.ui.playerpip.PlayerPipController

@Composable
internal fun rememberPlayerPip(
    viewModel: PlayerViewModel,
    eligible: Boolean,
    playing: Boolean,
    ratio: Float,
    bounds: Rect?,
    onClose: () -> Unit,
): PlayerPipController? {
    val context = LocalContext.current
    val pip = remember(context) { context.playerActivity()?.playerPip }
    val close by rememberUpdatedState(onClose)
    var binding by remember(pip, viewModel) { mutableStateOf<Any?>(null) }
    DisposableEffect(pip, viewModel) {
        val registration = pip?.bind(
            onPause = viewModel::pause,
            onPlay = viewModel::play,
            onClose = { close() },
        )
        binding = registration
        onDispose {
            registration?.let { pip?.unbind(it) }
        }
    }
    SideEffect {
        binding?.let { pip?.update(it, eligible, playing, ratio, bounds) }
    }
    return pip
}

private fun Context.playerActivity(): DimodoriActivity? = when (this) {
    is DimodoriActivity -> this
    is ContextWrapper -> if (baseContext !== this) baseContext.playerActivity() else null
    else -> null
}
