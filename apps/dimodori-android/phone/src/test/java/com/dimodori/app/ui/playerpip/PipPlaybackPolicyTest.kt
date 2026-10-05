package com.dimodori.app.ui.playerpip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PipPlaybackPolicyTest {
    @Test
    fun autoEntryRequiresLocalVideoPlayingAndPermission() {
        for (eligible in listOf(false, true)) {
            for (playing in listOf(false, true)) {
                for (allowed in listOf(false, true)) {
                    assertEquals(
                        eligible && playing && allowed,
                        PipPlaybackPolicy.canAutoEnter(eligible, playing, allowed),
                    )
                }
            }
        }
    }

    @Test
    fun visiblePipAndPendingEntryDoNotPausePlayback() {
        assertTrue(PipPlaybackPolicy.keepOnPause(true, false, false))
        assertTrue(PipPlaybackPolicy.keepOnPause(false, true, false))
        assertTrue(PipPlaybackPolicy.keepOnPause(false, false, true))
        assertFalse(PipPlaybackPolicy.keepOnPause(false, false, false))
    }

    @Test
    fun invalidAspectFallsBackAndExtremeVideosStayInsideSystemLimits() {
        for (ratio in listOf(Float.NaN, Float.POSITIVE_INFINITY, 0f, -1f)) {
            assertEquals(16f / 9f, PipPlaybackPolicy.aspectRatio(ratio), 0.0001f)
        }
        assertEquals(0.42f, PipPlaybackPolicy.aspectRatio(0.1f), 0.0001f)
        assertEquals(2.39f, PipPlaybackPolicy.aspectRatio(4f), 0.0001f)
        assertEquals(4f / 3f, PipPlaybackPolicy.aspectRatio(4f / 3f), 0.0001f)
    }
}
