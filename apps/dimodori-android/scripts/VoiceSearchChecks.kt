package com.dimodori.app.tv.ui.screens.dashboard.search

import com.dimodori.app.tv.ui.screens.detail.TrackSelectorActivation

/** Executes production request policy and the exact full-press gate used by the controls. */
fun main() {
    var checks = 0
    fun verify(value: Boolean) {
        check(value) { "Voice search check ${checks + 1} failed" }
        checks++
    }
    val a = VoiceSearchScope("server-a", "viewer-a")
    val b = VoiceSearchScope("server-b", "viewer-a")
    val anotherUser = VoiceSearchScope("server-a", "viewer-b")
    val request = VoiceSearchRequest()
    verify(request.complete(a, -1, listOf("Ignored")) == VoiceSearchOutcome.Discarded)
    verify(request.begin(a))
    verify(!request.begin(a))
    verify(!request.begin(b))
    verify(request.complete(a, -1, listOf("  Harry Potter  ")) == VoiceSearchOutcome.Query("Harry Potter"))
    verify(request.complete(a, -1, listOf("Duplicate")) == VoiceSearchOutcome.Discarded)
    verify(request.begin(a))
    verify(request.complete(a, 0, listOf("Do not use cancelled text")) == VoiceSearchOutcome.Cancelled)
    verify(request.begin(a))
    verify(request.complete(a, -1, null) == VoiceSearchOutcome.Empty)
    verify(request.begin(a))
    verify(request.complete(a, -1, emptyList()) == VoiceSearchOutcome.Empty)
    verify(request.begin(a))
    verify(request.complete(a, -1, listOf(" ", "\n")) == VoiceSearchOutcome.Empty)
    verify(request.begin(a))
    verify(request.complete(a, -1, listOf("", "  Dragón Ball Z ", "Other")) ==
        VoiceSearchOutcome.Query("Dragón Ball Z"))
    verify(request.begin(a))
    verify(request.complete(a, Int.MIN_VALUE, null) == VoiceSearchOutcome.Failed)
    verify(request.begin(a)) // Can retry after a launch error / missing recognizer.
    verify(request.complete(b, -1, listOf("Old server")) == VoiceSearchOutcome.Discarded)
    verify(request.begin(a))
    verify(request.complete(anotherUser, -1, listOf("Old account")) == VoiceSearchOutcome.Discarded)
    verify(request.begin(a))
    request.invalidate() // Leaving a screen, or A -> B -> A before callback.
    verify(!request.begin(b)) // Old activity still owns the pending result.
    verify(request.complete(a, -1, listOf("Late result")) == VoiceSearchOutcome.Discarded)
    verify(request.begin(b))
    verify(request.complete(b, -1, listOf("New request")) == VoiceSearchOutcome.Query("New request"))
    verify(request.begin(a))
    verify(request.complete(VoiceSearchScope(null, null), -1, listOf("After logout")) ==
        VoiceSearchOutcome.Discarded)

    val press = TrackSelectorActivation()
    val center = 23
    val enter = 66
    verify(!press.keyUp(center, false)) // Orphan release does not launch.
    press.keyDown(center, 1)
    verify(!press.keyUp(center, false)) // A repeat without the initial press.
    press.keyDown(center, 0)
    repeat(12) { press.keyDown(center, it + 1) }
    verify(press.keyUp(center, false)) // Exactly one activation, on release.
    verify(!press.keyUp(center, false))
    press.keyDown(center, 0)
    verify(!press.keyUp(center, true))
    press.keyDown(center, 0)
    press.reset() // Focus moved away while held.
    verify(!press.keyUp(center, false))
    press.keyDown(center, 0)
    verify(!press.keyUp(enter, false))
    press.keyDown(enter, 0)
    verify(press.keyUp(enter, false))

    // Non-query outcomes never touch the user's previous query.
    var query = "Previous title"
    listOf(VoiceSearchOutcome.Cancelled, VoiceSearchOutcome.Empty,
        VoiceSearchOutcome.Failed, VoiceSearchOutcome.Discarded).forEach { outcome ->
        if (outcome is VoiceSearchOutcome.Query) query = outcome.text
        verify(query == "Previous title")
    }
    println("$checks production voice-request / full-press checks passed.")
}
