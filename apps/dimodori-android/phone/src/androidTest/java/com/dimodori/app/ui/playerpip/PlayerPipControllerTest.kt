package com.dimodori.app.ui.playerpip

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Framework/controller callback contracts on a debug fixture, not media tests.
 * Real video continuity/surfaces still require the device acceptance matrix.
 */
@RunWith(AndroidJUnit4::class)
class PlayerPipControllerTest {
    @Test
    fun ineligibleOrPausedPlaybackDoesNotAutoEnterAndReturnDoesNotAutoPlay() {
        ActivityScenario.launch(PipTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val controller = PlayerPipController(activity)
                var pauses = 0
                var plays = 0
                val binding = controller.bind({ pauses++ }, { plays++ }, {})
                controller.update(binding, false, true, 16f / 9f, null)
                controller.onUserLeaveHint()
                controller.onPause()
                assertFalse(controller.showPipUi)
                assertEquals(1, pauses)
                controller.update(binding, true, false, 16f / 9f, null)
                controller.onUserLeaveHint()
                controller.onResume()
                assertFalse(controller.showPipUi)
                assertEquals(0, plays)
                controller.destroy()
            }
        }
    }

    @Test
    fun stopAfterPipClosesOnceAndRepeatedCallbacksCannotCloseAgain() {
        ActivityScenario.launch(PipTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val controller = PlayerPipController(activity)
                var closes = 0
                val binding = controller.bind({}, {}, { closes++ })
                controller.update(binding, true, true, 16f / 9f, null)
                controller.onModeChanged(true)
                controller.onStop()
                controller.onStop()
                controller.onModeChanged(false)
                controller.unbind(binding)
                assertEquals(1, closes)
                assertFalse(controller.showPipUi)
                controller.destroy()
            }
        }
    }

    @Test
    fun expandingWhileVisibleDoesNotCloseOrRestartPlayback() {
        ActivityScenario.launch(PipTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val controller = PlayerPipController(activity)
                var closes = 0
                var plays = 0
                controller.bind({}, { plays++ }, { closes++ })
                controller.onModeChanged(true)
                controller.onModeChanged(false)
                controller.onResume()
                assertEquals(0, closes)
                assertEquals(0, plays)
                controller.destroy()
            }
        }
    }

    @Test
    fun disposingOldScreenCannotDetachNewPlayback() {
        ActivityScenario.launch(PipTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val controller = PlayerPipController(activity)
                val oldBinding = controller.bind({}, {}, {})
                var closes = 0
                controller.bind({}, {}, { closes++ })
                controller.unbind(oldBinding)
                controller.onModeChanged(true)
                controller.onStop()
                assertEquals(1, closes)
                controller.destroy()
            }
        }
    }
}
