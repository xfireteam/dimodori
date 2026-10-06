package com.dimodori.app.ui.screens.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Compose after the inner NavHost to override tab-history Back, not dialogs. */
@Composable
internal fun DashboardHomeBackHandler(
    route: String?,
    homeRoute: String,
    hasOverlay: Boolean,
    onHome: () -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state by lifecycle.currentStateFlow.collectAsState()
    BackHandler(
        enabled = shouldReturnDashboardHome(
            route, homeRoute, state == Lifecycle.State.RESUMED, hasOverlay,
        ),
        onBack = onHome,
    )
}
