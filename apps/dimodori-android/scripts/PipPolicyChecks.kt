package com.dimodori.app.ui.playerpip

/** Executable checks of the actual production policy, without an Android SDK. */
fun main() {
    var checks = 0
    for (eligible in listOf(false, true)) {
        for (playing in listOf(false, true)) {
            for (allowed in listOf(false, true)) {
                check(PipPlaybackPolicy.canAutoEnter(eligible, playing, allowed) ==
                    (eligible && playing && allowed))
                checks++
            }
        }
    }
    for (inPip in listOf(false, true)) {
        for (entering in listOf(false, true)) {
            for (autoEnter in listOf(false, true)) {
                check(PipPlaybackPolicy.keepOnPause(inPip, entering, autoEnter) ==
                    (inPip || entering || autoEnter))
                checks++
            }
        }
    }
    for (ratio in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0f, -1f)) {
        check(PipPlaybackPolicy.aspectRatio(ratio) == 16f / 9f)
        checks++
    }
    for (ratio in listOf(0.1f, 0.42f, 1f, 4f / 3f, 16f / 9f, 2.39f, 4f)) {
        val sanitized = PipPlaybackPolicy.aspectRatio(ratio)
        // Test the same integer conversion used by the Android params builder.
        val rationalValue = (sanitized * 10000).toInt().toDouble() / 10000
        check(rationalValue >= 1.0 / 2.39 && rationalValue <= 2.39)
        if (ratio in 0.42f..2.39f) check(sanitized == ratio)
        checks++
    }
    println("$checks production PiP policy checks passed (not Android lifecycle/media tests).")
}
