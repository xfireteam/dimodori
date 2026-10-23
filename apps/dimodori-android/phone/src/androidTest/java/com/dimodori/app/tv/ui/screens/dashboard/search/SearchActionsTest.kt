package com.dimodori.app.tv.ui.screens.dashboard.search

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dimodori.app.ui.playerpip.PipTestActivity
import com.jellycine.shared.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Native UI regression checks. Requires an Android SDK + emulator/device. */
@RunWith(AndroidJUnit4::class)
class SearchActionsTest {
    @get:Rule val compose = createAndroidComposeRule<PipTestActivity>()
    private var searches = 0
    private var dictations = 0
    private val searchLabel get() = compose.activity.getString(R.string.search)
    private val voiceLabel get() = compose.activity.getString(R.string.tv_voice_search)

    private fun showActions() {
        compose.setContent {
            MaterialTheme {
                val search = remember { FocusRequester() }
                val voice = remember { FocusRequester() }
                Column {
                    SearchActions(
                        searchFocusRequester = search,
                        voiceFocusRequester = voice,
                        onSearch = { searches++ },
                        onVoice = { dictations++ },
                        onDown = {},
                    )
                    Text("Search fixture")
                }
            }
        }
        compose.onNodeWithContentDescription(searchLabel)
            .performSemanticsAction(SemanticsActions.RequestFocus) { it() }
    }

    private fun press(key: Int) {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(key)
        compose.waitForIdle()
    }

    @Test
    fun equallySizedControlsHaveOneFocusTargetAndNavigateBothWays() {
        showActions()
        val search = compose.onNodeWithContentDescription(searchLabel)
        val voice = compose.onNodeWithContentDescription(voiceLabel)
        search.assertIsFocused()
        search.assertWidthIsEqualTo(voice.getUnclippedBoundsInRoot().width)
        search.assertHeightIsEqualTo(voice.getUnclippedBoundsInRoot().height)
        press(KeyEvent.KEYCODE_DPAD_RIGHT)
        voice.assertIsFocused()
        press(KeyEvent.KEYCODE_DPAD_LEFT)
        search.assertIsFocused()
        press(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals(1, searches)
        assertEquals(0, dictations)
    }

    @Test
    fun heldPressOnlyLaunchesOnceAfterReleaseAndCancelledPressDoesNotLaunch() {
        showActions()
        press(KeyEvent.KEYCODE_DPAD_RIGHT)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val downTime = SystemClock.uptimeMillis()
        fun send(action: Int, repeat: Int = 0, flags: Int = 0) {
            instrumentation.sendKeySync(KeyEvent(
                downTime, SystemClock.uptimeMillis(), action,
                KeyEvent.KEYCODE_DPAD_CENTER, repeat, 0,
                -1, 0, flags,
            ))
            compose.waitForIdle()
        }
        send(KeyEvent.ACTION_DOWN)
        repeat(4) { send(KeyEvent.ACTION_DOWN, repeat = it + 1) }
        assertEquals(0, dictations)
        send(KeyEvent.ACTION_UP)
        assertEquals(1, dictations)
        send(KeyEvent.ACTION_DOWN)
        send(KeyEvent.ACTION_UP, flags = KeyEvent.FLAG_CANCELED)
        assertEquals(1, dictations)
    }
}
