package com.dimodori.app.tv.ui.screens.dashboard.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jellycine.shared.R

/**
 * The system activity owns microphone permissions and listening. Do not preflight
 * resolveActivity: package visibility can hide an otherwise launchable recognizer.
 */
@Composable
internal fun rememberVoiceSearch(
    currentScope: () -> VoiceSearchScope,
    onQuery: (String) -> Unit,
    onReturned: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val locale = LocalConfiguration.current.locales[0].toLanguageTag()
    val request = remember { VoiceSearchRequest() }
    val currentOnQuery by rememberUpdatedState(onQuery)
    val currentOnReturned by rememberUpdatedState(onReturned)
    // A -> B -> A must also invalidate A's old dictation.
    val scope = currentScope()
    DisposableEffect(scope) {
        onDispose { request.invalidate() }
    }
    DisposableEffect(request) {
        onDispose { request.invalidate() }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) request.invalidate()
        val outcome = try {
            request.complete(
                currentScope(), result.resultCode,
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.filterIsInstance<String>(),
            )
        } catch (_: RuntimeException) {
            // Malformed extras from a device's recognizer must not crash search.
            request.complete(currentScope(), Int.MIN_VALUE, null)
        }
        when (outcome) {
            is VoiceSearchOutcome.Query -> currentOnQuery(outcome.text)
            VoiceSearchOutcome.Empty -> Toast.makeText(
                context, R.string.tv_voice_search_empty, Toast.LENGTH_LONG
            ).show()
            VoiceSearchOutcome.Failed -> Toast.makeText(
                context, R.string.tv_voice_search_failed, Toast.LENGTH_LONG
            ).show()
            VoiceSearchOutcome.Cancelled, VoiceSearchOutcome.Discarded -> Unit
        }
        if (outcome != VoiceSearchOutcome.Discarded) currentOnReturned()
    }

    return {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
            request.begin(currentScope())
        ) {
            try {
                launcher.launch(
                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
                        .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                        .putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.tv_voice_search_prompt))
                )
            } catch (_: ActivityNotFoundException) {
                request.complete(currentScope(), Int.MIN_VALUE, null)
                Toast.makeText(context, R.string.tv_voice_search_unavailable, Toast.LENGTH_LONG).show()
                currentOnReturned()
            } catch (_: RuntimeException) {
                request.complete(currentScope(), Int.MIN_VALUE, null)
                Toast.makeText(context, R.string.tv_voice_search_failed, Toast.LENGTH_LONG).show()
                currentOnReturned()
            }
        }
    }
}
