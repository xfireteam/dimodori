import com.dimodori.app.tv.ui.screens.detail.TrackSelectorActivation

/** Runs the production gate, without Android, Compose, or injected TV events. */
fun main() {
    var checks = 0
    fun verify(condition: Boolean) {
        check(condition)
        checks++
    }
    for (key in listOf(23, 66, 160)) { // D-pad center, Enter, numpad Enter.
        val gate = TrackSelectorActivation()
        verify(!gate.keyUp(key, false)) // Stray release cannot select.
        gate.keyDown(key, 0)
        repeat(20) { gate.keyDown(key, it + 1) }
        verify(gate.keyUp(key, false)) // One activation after a long press.
        verify(!gate.keyUp(key, false))
        gate.keyDown(key, 3) // Opening press repeats arriving at a new popup.
        verify(!gate.keyUp(key, false))
        gate.keyDown(key, 0)
        verify(!gate.keyUp(key, true))
        gate.keyDown(key, 0)
        gate.reset() // Focus loss.
        verify(!gate.keyUp(key, false))
        gate.keyDown(key, 0)
        verify(!gate.keyUp(key + 1, false))
        gate.keyDown(key, 0)
        verify(gate.keyUp(key, false)) // Next genuine press still works.
    }
    println("$checks production activation checks passed (not native TV tests).")
}
