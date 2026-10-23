package com.dimodori.app.tv.ui.screens.dashboard.search

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dimodori.app.ui.playerpip.PipTestActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Uses the actual launcher adapter with a fake system provider; never records audio. */
@RunWith(AndroidJUnit4::class)
class VoiceSearchTest {
    @get:Rule val compose = createAndroidComposeRule<PipTestActivity>()
    private var scope by mutableStateOf(VoiceSearchScope("a", "viewer"))
    private var visible by mutableStateOf(true)
    private var query = "Previous title"
    private var returns = 0
    private var launches = 0
    private var launchCode = 0
    private var unavailable = false
    private var launchedIntent: Intent? = null
    private val registry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            launches++
            if (unavailable) throw ActivityNotFoundException("Test: no voice provider")
            launchCode = requestCode
            launchedIntent = contract.createIntent(compose.activity, input)
        }
    }

    private fun showVoice() {
        compose.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides
                object : ActivityResultRegistryOwner {
                    override val activityResultRegistry = registry
                }
            ) {
                if (visible) {
                    val voice = rememberVoiceSearch(
                        currentScope = { scope },
                        onQuery = { query = it },
                        onReturned = { returns++ },
                    )
                    Button(onClick = voice) { Text("Dictate") }
                }
            }
        }
    }

    private fun launch() {
        compose.onNodeWithText("Dictate").performClick()
        compose.waitForIdle()
    }

    private fun result(code: Int, phrases: ArrayList<String>? = null) {
        compose.runOnIdle {
            registry.dispatchResult(
                launchCode, code,
                phrases?.let { Intent().putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, it) },
            )
        }
        compose.waitForIdle()
    }

    @Test
    fun validTitleUsesNativeIntentAndOnlyOnePendingRequest() {
        showVoice()
        launch()
        launch()
        assertEquals(1, launches)
        assertEquals(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, launchedIntent?.action)
        assertEquals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            launchedIntent?.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL))
        assertFalse(launchedIntent?.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE).isNullOrBlank())
        result(Activity.RESULT_OK, arrayListOf("  Harry Potter "))
        assertEquals("Harry Potter", query)
        assertEquals(1, returns)
    }

    @Test
    fun cancelEmptyAndProviderErrorKeepExistingQueryAndReturnToControl() {
        showVoice()
        launch()
        result(Activity.RESULT_CANCELED, arrayListOf("Cancelled text"))
        assertEquals("Previous title", query)
        launch()
        result(Activity.RESULT_OK, arrayListOf(" "))
        assertEquals("Previous title", query)
        launch()
        result(Activity.RESULT_FIRST_USER)
        assertEquals("Previous title", query)
        assertEquals(3, returns)
    }

    @Test
    fun missingProviderDoesNotCrashAndAllowsRetry() {
        showVoice()
        unavailable = true
        launch()
        assertEquals("Previous title", query)
        assertEquals(1, returns)
        unavailable = false
        launch()
        result(Activity.RESULT_OK, arrayListOf("Retry"))
        assertEquals("Retry", query)
        assertEquals(2, returns)
    }

    @Test
    fun changedServerAndRoundTripServerChangeDiscardOldDictation() {
        showVoice()
        launch()
        compose.runOnIdle { scope = VoiceSearchScope("b", "viewer") }
        compose.waitForIdle()
        result(Activity.RESULT_OK, arrayListOf("Old server"))
        assertEquals("Previous title", query)
        assertEquals(0, returns)
        launch()
        compose.runOnIdle { scope = VoiceSearchScope("a", "viewer") }
        compose.waitForIdle()
        compose.runOnIdle { scope = VoiceSearchScope("b", "viewer") }
        compose.waitForIdle()
        result(Activity.RESULT_OK, arrayListOf("Round trip"))
        assertEquals("Previous title", query)
        assertEquals(0, returns)
    }

    @Test
    fun leavingAndRecreatingScreenDoesNotApplyOldResult() {
        showVoice()
        launch()
        compose.runOnIdle { visible = false }
        compose.waitForIdle()
        result(Activity.RESULT_OK, arrayListOf("Abandoned screen"))
        compose.runOnIdle { visible = true }
        compose.waitForIdle()
        assertEquals("Previous title", query)
        assertEquals(0, returns)
        launch()
        result(Activity.RESULT_OK, arrayListOf("New screen"))
        assertEquals("New screen", query)
    }
}
