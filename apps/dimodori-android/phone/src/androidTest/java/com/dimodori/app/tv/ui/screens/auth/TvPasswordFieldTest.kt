package com.dimodori.app.tv.ui.screens.auth

import android.os.SystemClock
import android.view.KeyEvent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dimodori.app.ui.playerpip.PipTestActivity
import com.jellycine.shared.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests the actual TV LoginForm with fixture-only text, never server credentials. */
@RunWith(AndroidJUnit4::class)
class TvPasswordFieldTest {
    @get:Rule val compose = createAndroidComposeRule<PipTestActivity>()
    private var password by mutableStateOf("sample-123")
    private var username by mutableStateOf("viewer")
    private var busy by mutableStateOf(false)
    private var quickBusy by mutableStateOf(false)
    private var submissions = 0
    private var quickConnections = 0
    private var passwordWrites = 0
    private val showLabel get() = compose.activity.getString(R.string.auth_show_password)
    private val hideLabel get() = compose.activity.getString(R.string.auth_hide_password)
    private fun field() = compose.onNode(
        hasSetTextAction() and hasText(compose.activity.getString(R.string.password))
    )

    private fun showForm() {
        compose.setContent {
            MaterialTheme {
                LoginForm(
                    username = username,
                    password = password,
                    isLoading = busy,
                    errorMessage = null,
                    showQuickConnect = true,
                    isQuickConnectLoading = quickBusy,
                    quickConnectCode = null,
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it; passwordWrites++ },
                    onLogin = { submissions++ },
                    onQuickConnect = { quickConnections++ },
                    modifier = Modifier.width(420.dp),
                )
            }
        }
    }

    private fun hideKeyboard() {
        compose.runOnIdle {
            val activity = compose.activity
            WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                .hide(WindowInsetsCompat.Type.ime())
        }
        compose.waitUntil(5_000) {
            ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) != true
        }
        compose.waitForIdle()
    }

    private fun press(key: Int) {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(key)
        compose.waitForIdle()
    }

    private fun focusPassword() {
        field().performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        hideKeyboard()
    }

    @Test
    fun remoteReachesEyeTogglesAndReturnsToSameCaretWithoutSubmitting() {
        showForm()
        compose.onNodeWithContentDescription(showLabel).assertExists()
        focusPassword()
        field().performTextInputSelection(TextRange(3))
        hideKeyboard()
        press(KeyEvent.KEYCODE_DPAD_RIGHT)
        compose.onNodeWithContentDescription(showLabel).assertIsFocused()
        press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.onNodeWithContentDescription(hideLabel).assertIsFocused()
        assertEquals("sample-123", password)
        assertEquals("viewer", username)
        assertEquals(0, passwordWrites)
        assertEquals(0, submissions)
        assertEquals(0, quickConnections)
        press(KeyEvent.KEYCODE_DPAD_LEFT)
        field().assertIsFocused()
        field().performTextInput("X")
        assertEquals("samXple-123", password)
        hideKeyboard()
        press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.onNodeWithContentDescription(hideLabel).assertIsFocused()
        press(KeyEvent.KEYCODE_ENTER)
        compose.onNodeWithContentDescription(showLabel).assertIsFocused()
        press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.onNodeWithText(compose.activity.getString(R.string.auth_sign_in)).assertIsFocused()
        assertEquals(0, submissions)
    }

    @Test
    fun heldAndCancelledPressesDoNotToggleRepeatedlyOrSubmit() {
        showForm()
        focusPassword()
        press(KeyEvent.KEYCODE_DPAD_DOWN)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val downTime = SystemClock.uptimeMillis()
        fun send(action: Int, repeat: Int = 0, flags: Int = 0) {
            instrumentation.sendKeySync(KeyEvent(
                downTime, SystemClock.uptimeMillis(), action,
                KeyEvent.KEYCODE_DPAD_CENTER, repeat, 0, -1, 0, flags,
            ))
            compose.waitForIdle()
        }
        send(KeyEvent.ACTION_DOWN)
        repeat(5) { send(KeyEvent.ACTION_DOWN, repeat = it + 1) }
        compose.onNodeWithContentDescription(showLabel).assertIsFocused()
        send(KeyEvent.ACTION_UP)
        compose.onNodeWithContentDescription(hideLabel).assertIsFocused()
        send(KeyEvent.ACTION_DOWN)
        send(KeyEvent.ACTION_UP, flags = KeyEvent.FLAG_CANCELED)
        compose.onNodeWithContentDescription(hideLabel).assertIsFocused()
        assertEquals("sample-123", password)
        assertEquals(0, submissions)
    }

    @Test
    fun busyStatesDisableEyeAndBlankUsernameCanAdvanceToQuickConnect() {
        username = ""
        showForm()
        focusPassword()
        press(KeyEvent.KEYCODE_DPAD_RIGHT)
        press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.onNodeWithText(compose.activity.getString(R.string.auth_quick_connect)).assertIsFocused()
        compose.runOnIdle { busy = true }
        compose.onNodeWithContentDescription(showLabel).assertIsNotEnabled()
        compose.runOnIdle { busy = false; quickBusy = true }
        compose.onNodeWithContentDescription(showLabel).assertIsNotEnabled()
        assertEquals("sample-123", password)
        assertEquals(0, submissions)
        assertEquals(0, quickConnections)
    }
}
