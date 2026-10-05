package com.dimodori.app.ui.playerpip

/** Framework-independent rules shared by both local playback engines. */
internal object PipPlaybackPolicy {
    fun canAutoEnter(eligible: Boolean, playing: Boolean, allowed: Boolean): Boolean =
        eligible && playing && allowed

    fun keepOnPause(inPip: Boolean, entering: Boolean, autoEnter: Boolean): Boolean =
        inPip || entering || autoEnter

    fun aspectRatio(value: Float): Float =
        // Stay just inside the minimum so Rational integer rounding is safe.
        if (value.isFinite() && value > 0f) value.coerceIn(0.42f, 2.39f)
        else 16f / 9f
}
