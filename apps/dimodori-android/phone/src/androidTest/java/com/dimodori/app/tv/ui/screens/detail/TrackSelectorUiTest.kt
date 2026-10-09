package com.dimodori.app.tv.ui.screens.detail

import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dimodori.app.ui.playerpip.PipTestActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Injects platform events so key routing across the popup window is tested. */
@RunWith(AndroidJUnit4::class)
class TrackSelectorUiTest {
    @get:Rule val compose = createAndroidComposeRule<PipTestActivity>()
    private var callbacks = 0
    private var parentBacks = 0
    private var selected by mutableStateOf("Spanish AC3")
    private var lastChoice: String? = null
    private var downTime = 0L

    private fun show(
        label: String = "Audio",
        options: List<String> = listOf("Spanish AC3", "English AAC", "French"),
    ) {
        compose.setContent {
            MaterialTheme {
                BackHandler { parentBacks++ }
                Column {
                    OptionSelectorRow(label, selected, options) {
                        selected = it
                        lastChoice = it
                        callbacks++
                    }
                }
            }
        }
        compose.onNodeWithContentDescription("Seleccionar $label")
            .performSemanticsAction(SemanticsActions.RequestFocus) { it() }
    }

    private fun send(key: Int, action: Int, repeats: Int = 0, flags: Int = 0) {
        if (action == KeyEvent.ACTION_DOWN && repeats == 0) downTime = SystemClock.uptimeMillis()
        InstrumentationRegistry.getInstrumentation().sendKeySync(
            KeyEvent(
                downTime, SystemClock.uptimeMillis(), action, key, repeats, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, flags, InputDevice.SOURCE_DPAD,
            )
        )
        compose.waitForIdle()
    }

    private fun press(key: Int) {
        send(key, KeyEvent.ACTION_DOWN)
        send(key, KeyEvent.ACTION_UP)
    }

    private fun option(text: String) =
        compose.onNode(hasText(text) and hasAnyAncestor(isPopup()))

    @Test
    fun openingLongPressDoesNotDismissOrChooseAndArrowSelectionWorks() {
        show()
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN)
        option("Spanish AC3").assertDoesNotExist()
        repeat(3) { send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN, it + 1) }
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP)
        option("Spanish AC3").assertIsDisplayed().assertIsFocused()
        assertEquals(0, callbacks)
        press(KeyEvent.KEYCODE_DPAD_DOWN)
        option("English AAC").assertIsFocused()
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN)
        repeat(3) { send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN, it + 1) }
        assertEquals(0, callbacks)
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP)
        assertEquals(1, callbacks)
        assertEquals("English AAC", lastChoice)
        option("English AAC").assertDoesNotExist()
        compose.onNodeWithContentDescription("Seleccionar Audio").assertIsFocused()
    }

    @Test
    fun backCancelsWithoutChangingChoiceOrLeavingDetailAndCanReopen() {
        show()
        press(KeyEvent.KEYCODE_ENTER)
        option("Spanish AC3").assertIsFocused()
        press(KeyEvent.KEYCODE_DPAD_DOWN)
        press(KeyEvent.KEYCODE_BACK)
        option("English AAC").assertDoesNotExist()
        assertEquals(0, callbacks)
        assertEquals(0, parentBacks)
        assertEquals("Spanish AC3", selected)
        compose.onNodeWithContentDescription("Seleccionar Audio").assertIsFocused()
        press(KeyEvent.KEYCODE_NUMPAD_ENTER)
        option("Spanish AC3").assertIsFocused()
    }

    @Test
    fun repeatedOpeningEventsAtAnOptionCannotSelectIt() {
        show()
        press(KeyEvent.KEYCODE_DPAD_CENTER)
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN, 1)
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP)
        option("Spanish AC3").assertIsDisplayed()
        assertEquals(0, callbacks)
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN)
        send(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP, flags = KeyEvent.FLAG_CANCELED)
        option("Spanish AC3").assertIsDisplayed()
        assertEquals(0, callbacks)
    }

    @Test
    fun longListFocusesCurrentTrackScrollsAndCanDisableSubtitles() {
        val options = listOf("Off") + (1..25).map { "Idioma $it" }
        selected = "Idioma 24"
        show("Subtítulos", options)
        press(KeyEvent.KEYCODE_DPAD_CENTER)
        option("Idioma 24").assertIsDisplayed().assertIsFocused()
        repeat(24) { press(KeyEvent.KEYCODE_DPAD_UP) }
        option("Desactivado").assertIsDisplayed().assertIsFocused()
        press(KeyEvent.KEYCODE_DPAD_CENTER)
        assertEquals("Off", lastChoice)
        assertEquals(1, callbacks)
        compose.onNodeWithContentDescription("Seleccionar Subtítulos").assertIsFocused()
    }

    @Test
    fun sharedVideoSelectorCallsTheChosenVersionOnlyOnce() {
        selected = "1080p / versión A"
        show("Vídeo", listOf("1080p / versión A", "4K / versión B"))
        press(KeyEvent.KEYCODE_DPAD_CENTER)
        press(KeyEvent.KEYCODE_DPAD_DOWN)
        press(KeyEvent.KEYCODE_ENTER)
        assertEquals("4K / versión B", lastChoice)
        assertEquals(1, callbacks)
        option("4K / versión B").assertDoesNotExist()
    }
}
