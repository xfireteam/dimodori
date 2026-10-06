package com.dimodori.app.ui.screens.dashboard

internal fun shouldReturnDashboardHome(
    route: String?,
    homeRoute: String,
    resumed: Boolean,
    hasOverlay: Boolean,
): Boolean = resumed && !hasOverlay && route != null && route != homeRoute
