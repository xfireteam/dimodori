package com.dimodori.app.tv.ui.screens.detail

/**
 * Activate only after a complete press/release. Opening a focusable popup on
 * KeyDown lets the opening key's repeat/KeyUp reach its newly focused option.
 */
internal class TrackSelectorActivation {
    private var pressedKey: Int? = null

    fun keyDown(keyCode: Int, repeatCount: Int) {
        if (repeatCount == 0 && pressedKey == null) pressedKey = keyCode
    }

    fun keyUp(keyCode: Int, canceled: Boolean): Boolean {
        val activate = pressedKey == keyCode && !canceled
        reset()
        return activate
    }

    fun reset() {
        pressedKey = null
    }
}
