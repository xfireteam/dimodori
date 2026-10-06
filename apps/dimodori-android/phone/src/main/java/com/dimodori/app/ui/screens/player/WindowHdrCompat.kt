package com.dimodori.app.ui.screens.player

import android.os.Build
import android.util.Log
import android.view.Window

/**
 * Do not put a direct call to the API-35 method in dex. Upstream's Android-14
 * close crash also involved R8 outlining the guarded call during disposal.
 */
internal fun setDesiredHdrHeadroomCompat(window: Window, headroom: Float) {
    if (!supportsWindowHdrHeadroom(Build.VERSION.SDK_INT)) return
    runCatching {
        Window::class.java
            .getMethod("setDesiredHdrHeadroom", Float::class.javaPrimitiveType)
            .invoke(window, headroom)
    }.onFailure {
        // HDR headroom is optional: an OEM failure must not prevent cleanup.
        Log.w("PlayerHdr", "Window HDR headroom unavailable", it)
    }
}
