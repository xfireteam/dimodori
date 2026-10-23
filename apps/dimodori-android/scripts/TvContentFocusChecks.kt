package com.dimodori.app.tv.ui.focus

/** Runs the production policy without an Android SDK or Compose runtime. */
fun main() {
    var checks = 0
    fun expect(type: String?, focused: Boolean, visible: Boolean) {
        check(showsTvContentFocusOutline(type, focused) == visible) {
            "Unexpected outline for type=$type, focused=$focused"
        }
        checks++
    }
    listOf("Movie", "movie", "MOVIE", "Episode", "episode", "EPISODE").forEach {
        expect(it, true, true)
        expect(it, false, false)
    }
    listOf(null, "", "Series", "tv", "Season", "BoxSet", "Person",
        "Folder", "CollectionFolder", "Audio", "MusicAlbum", "Trailer").forEach {
        expect(it, true, false)
        expect(it, false, false)
    }
    val cards = listOf("Movie", "Episode", "Series")
    // Moving focus leaves no persistent selection ring on the previous card.
    for (focusedIndex in cards.indices) {
        val outlines = cards.mapIndexed { index, type ->
            showsTvContentFocusOutline(type, index == focusedIndex)
        }
        check(outlines == cards.indices.map { it == focusedIndex && it < 2 })
        checks++
    }
    println("TV content focus policy: $checks checks passed")
}
