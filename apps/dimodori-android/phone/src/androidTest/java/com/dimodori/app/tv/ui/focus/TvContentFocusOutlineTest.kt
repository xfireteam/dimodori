package com.dimodori.app.tv.ui.focus

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dimodori.app.ui.playerpip.PipTestActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Native rendering/input checks; requires a device/emulator, not a web preview. */
@RunWith(AndroidJUnit4::class)
class TvContentFocusOutlineTest {
    @get:Rule val compose = createAndroidComposeRule<PipTestActivity>()
    private var clicks = 0

    private fun showCards() {
        compose.setContent {
            MaterialTheme {
                Row {
                    listOf("Movie", "Episode", "Series").forEach { type ->
                        val focus = rememberTvContentFocus()
                        Column(
                            Modifier.testTag(type)
                                .tvContentFocusObserver(focus)
                                .clickable { clicks++ }
                        ) {
                            Box(
                                Modifier.size(100.dp, 80.dp).testTag("$type-image")
                                    .tvContentFocusOutline(type, focus, RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                            )
                            Text(type)
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag("Movie")
            .performSemanticsAction(SemanticsActions.RequestFocus) { it() }
    }

    private fun press(key: Int) {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(key)
        compose.waitForIdle()
    }

    private fun assertWhiteEdge(type: String, expected: Boolean) {
        val pixels = compose.onNodeWithTag("$type-image", useUnmergedTree = true)
            .captureToImage().toPixelMap()
        // Sample the middle of the left edge, away from rounded corners/text.
        val white = (0 until pixels.width / 8).any { x ->
            val pixel = pixels[x, pixels.height / 2]
            pixel.red > .95f && pixel.green > .95f && pixel.blue > .95f
        }
        assertEquals("$type outline", expected, white)
    }

    @Test
    fun remoteMovesOutlineWithoutChangingBoundsOrClickBehavior() {
        showCards()
        val before = compose.onNodeWithTag("Movie").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithTag("Movie").assertIsFocused()
        assertWhiteEdge("Movie", true)
        assertWhiteEdge("Episode", false)
        press(KeyEvent.KEYCODE_DPAD_RIGHT)
        compose.onNodeWithTag("Episode").assertIsFocused()
        assertWhiteEdge("Movie", false)
        assertWhiteEdge("Episode", true)
        assertEquals(before, compose.onNodeWithTag("Movie").fetchSemanticsNode().boundsInRoot)
        press(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals(1, clicks)
        press(KeyEvent.KEYCODE_DPAD_RIGHT)
        compose.onNodeWithTag("Series").assertIsFocused()
        assertWhiteEdge("Episode", false)
        assertWhiteEdge("Series", false)
        assertTrue(clicks == 1)
    }
}
