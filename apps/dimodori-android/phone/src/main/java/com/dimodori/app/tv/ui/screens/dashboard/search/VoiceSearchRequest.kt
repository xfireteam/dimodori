package com.dimodori.app.tv.ui.screens.dashboard.search

/** No Android dependency: one dictation belongs to the screen and session that opened it. */
internal data class VoiceSearchScope(val serverId: String?, val username: String?)

internal sealed interface VoiceSearchOutcome {
    data class Query(val text: String) : VoiceSearchOutcome
    data object Cancelled : VoiceSearchOutcome
    data object Empty : VoiceSearchOutcome
    data object Failed : VoiceSearchOutcome
    data object Discarded : VoiceSearchOutcome
}

internal class VoiceSearchRequest {
    private var pendingScope: VoiceSearchScope? = null
    private var invalidated = false

    fun begin(scope: VoiceSearchScope): Boolean {
        if (pendingScope != null) return false
        pendingScope = scope
        invalidated = false
        return true
    }

    // Keep the request occupied until its result arrives, even after a scope change.
    fun invalidate() {
        invalidated = true
    }

    fun complete(
        scope: VoiceSearchScope,
        resultCode: Int,
        phrases: List<String>?,
    ): VoiceSearchOutcome {
        val launchedScope = pendingScope ?: return VoiceSearchOutcome.Discarded
        pendingScope = null
        if (invalidated || launchedScope != scope) return VoiceSearchOutcome.Discarded
        return when (resultCode) {
            0 -> VoiceSearchOutcome.Cancelled // Activity.RESULT_CANCELED
            -1 -> phrases?.firstOrNull { it.isNotBlank() }?.trim()
                ?.let(VoiceSearchOutcome::Query) ?: VoiceSearchOutcome.Empty
            else -> VoiceSearchOutcome.Failed
        }
    }
}
