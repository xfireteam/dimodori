package com.dimodori.app.ui.screens.player

/** Window HDR headroom is an Android 15 API, not an Android 14 API. */
internal fun supportsWindowHdrHeadroom(sdk: Int): Boolean = sdk >= 35
