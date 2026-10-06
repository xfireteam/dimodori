package com.dimodori.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Register before screen content so child selection/dialog/player handlers win.
 * A screen retained during navigation or paused in PiP must not intercept Back.
 */
@Composable
internal fun ScreenBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state by lifecycle.currentStateFlow.collectAsState()
    BackHandler(enabled = enabled && state == Lifecycle.State.RESUMED, onBack = onBack)
}
