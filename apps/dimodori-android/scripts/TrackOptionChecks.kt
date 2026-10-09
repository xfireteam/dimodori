import com.dimodori.app.tv.ui.screens.detail.*
import com.jellycine.data.model.MediaStream

/** Production mapping functions/model, without Android UI or serialization. */
fun main() {
    var checks = 0
    fun verify(actual: Any?, expected: Any?) {
        check(actual == expected) { "$actual != $expected" }
        checks++
    }
    val audio = listOf(
        MediaStream(type = "Audio", index = 9, displayTitle = "Spanish AC3"),
        MediaStream(type = "Video", index = 0, displayTitle = "1080p"),
        MediaStream(type = "Audio", index = 3, displayTitle = "Spanish AC3"),
    )
    verify(buildAudioOptions(audio), listOf("Spanish AC3", "Spanish AC3 (2)"))
    verify(AudioStreamIndex(audio, "Spanish AC3"), 3)
    verify(AudioStreamIndex(audio, "Spanish AC3 (2)"), 9)
    verify(AudioStreamIndex(audio, 9), "Spanish AC3 (2)")
    val subtitles = listOf(
        MediaStream(type = "Subtitle", index = 12, displayTitle = "Español", isExternal = true),
        MediaStream(type = "Subtitle", index = 4, displayTitle = "Español", isDefault = true),
    )
    verify(buildSubtitleOptions(subtitles), listOf("Off", "Español", "Español (2)"))
    verify(SubtitleStreamIndex(subtitles, "Off"), -1)
    verify(SubtitleStreamIndex(subtitles, "Español"), 4)
    verify(SubtitleStreamIndex(subtitles, "Español (2)"), 12)
    verify(SubtitleStreamIndex(subtitles, 12), "Español (2)")
    verify(SubtitleStreamIndex(subtitles, -1), "Off")
    verify(buildDefaultSubtitleOption(subtitles), "Español")
    verify(buildAudioOptions(emptyList()), emptyList<String>())
    verify(buildSubtitleOptions(emptyList()), listOf("Off"))
    verify(AudioStreamIndex(emptyList(), ""), null)
    val single = listOf(MediaStream(type = "Audio", index = 6, displayTitle = "English"))
    verify(buildAudioOptions(single), listOf("English"))
    verify(AudioStreamIndex(single, "English"), 6)
    println("$checks production track-mapping checks passed (not playback tests).")

    // Separate pre-existing issue, unrelated to opening/closing the popup.
    val untitled = listOf(
        MediaStream(type = "Audio", index = 2, displayTitle = null),
        MediaStream(type = "Audio", index = 8, displayTitle = "Español"),
    )
    val mapped = AudioStreamIndex(untitled, "Español")
    if (mapped != 8) {
        println("PRE-EXISTING GAP: untitled track shifts Español to index $mapped instead of 8.")
    }
}
