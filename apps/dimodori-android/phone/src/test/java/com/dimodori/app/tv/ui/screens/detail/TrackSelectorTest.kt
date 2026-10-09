package com.dimodori.app.tv.ui.screens.detail

import com.jellycine.data.model.MediaStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackSelectorTest {
    @Test
    fun fullPressAndLongPressActivateOnlyOnceOnRelease() {
        val gate = TrackSelectorActivation()
        gate.keyDown(23, 0)
        repeat(10) { gate.keyDown(23, it + 1) }
        assertTrue(gate.keyUp(23, false))
        assertFalse(gate.keyUp(23, false))
    }

    @Test
    fun strayCanceledAndFocusLostReleasesCannotSelect() {
        val gate = TrackSelectorActivation()
        assertFalse(gate.keyUp(23, false))
        gate.keyDown(23, 1)
        assertFalse(gate.keyUp(23, false))
        gate.keyDown(23, 0)
        assertFalse(gate.keyUp(23, true))
        gate.keyDown(23, 0)
        gate.reset()
        assertFalse(gate.keyUp(23, false))
        gate.keyDown(66, 0)
        assertFalse(gate.keyUp(160, false))
    }

    @Test
    fun duplicateAudioLabelsStillMapToSortedRealStreamIndexes() {
        val streams = listOf(
            MediaStream(type = "Audio", index = 9, displayTitle = "Spanish AC3"),
            MediaStream(type = "Video", index = 0, displayTitle = "1080p"),
            MediaStream(type = "Audio", index = 3, displayTitle = "Spanish AC3"),
        )
        assertEquals(listOf("Spanish AC3", "Spanish AC3 (2)"), buildAudioOptions(streams))
        assertEquals(3, AudioStreamIndex(streams, "Spanish AC3"))
        assertEquals(9, AudioStreamIndex(streams, "Spanish AC3 (2)"))
        assertEquals("Spanish AC3 (2)", AudioStreamIndex(streams, 9))
    }

    @Test
    fun externalAndDuplicateSubtitlesKeepOffAndRealIndexes() {
        val streams = listOf(
            MediaStream(type = "Subtitle", index = 12, displayTitle = "Español", isExternal = true),
            MediaStream(type = "Subtitle", index = 4, displayTitle = "Español", isDefault = true),
        )
        assertEquals(listOf("Off", "Español", "Español (2)"), buildSubtitleOptions(streams))
        assertEquals(-1, SubtitleStreamIndex(streams, "Off"))
        assertEquals(4, SubtitleStreamIndex(streams, "Español"))
        assertEquals(12, SubtitleStreamIndex(streams, "Español (2)"))
        assertEquals("Español (2)", SubtitleStreamIndex(streams, 12))
        assertEquals("Off", SubtitleStreamIndex(streams, -1))
        assertEquals("Español", buildDefaultSubtitleOption(streams))
    }

    @Test
    fun emptyAndSingleTrackListsKeepExistingBehavior() {
        assertEquals(emptyList<String>(), buildAudioOptions(emptyList()))
        assertEquals(listOf("Off"), buildSubtitleOptions(emptyList()))
        assertEquals(null, AudioStreamIndex(emptyList(), ""))
        assertEquals(-1, SubtitleStreamIndex(emptyList(), "Off"))
        val single = listOf(MediaStream(type = "Audio", index = 6, displayTitle = "English"))
        assertEquals(listOf("English"), buildAudioOptions(single))
        assertEquals(6, AudioStreamIndex(single, "English"))
    }
}
