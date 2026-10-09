#!/usr/bin/env python3
"""Source-wiring checks only: no Android, native focus, or playback execution."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
DETAIL = ROOT / "phone/src/main/java/com/dimodori/app/tv/ui/screens/detail"


class TrackSelectorContract(unittest.TestCase):
    def test_tv_selector_has_one_opening_mechanism(self):
        source = (DETAIL / "TrackSection.kt").read_text()
        for old in ("ExposedDropdownMenu", "menuAnchor(", "onExpandedChange"):
            self.assertNotIn(old, source)
        self.assertIn(".trackSelectorActivation { expanded = true }", source)
        self.assertIn("onClick = { expanded = true }", source)

    def test_popup_owns_focus_and_back_without_focus_loss_dismissal(self):
        source = (DETAIL / "TrackSection.kt").read_text()
        self.assertIn("PopupProperties(focusable = true)", source)
        self.assertIn("onDismissRequest = closeSelector", source)
        self.assertIn("optionFocusRequesters[index].requestFocus()", source)
        self.assertIn("anchorFocusRequester.requestFocus()", source)
        self.assertNotIn("onFocusChanged { expanded", source)

    def test_selection_closes_before_version_callback(self):
        source = (DETAIL / "TrackSection.kt").read_text()
        self.assertIn("closeSelector()\n                        onOptionSelected(option)", source)
        self.assertIn(".trackSelectorActivation(selectOption)", source)
        self.assertIn("onClick = selectOption", source)

    def test_both_key_phases_repeats_and_cancellation_are_processed(self):
        source = (DETAIL / "TrackSelectorActivationModifier.kt").read_text()
        for value in ("Key.Enter", "Key.NumPadEnter", "Key.DirectionCenter",
                      "KeyEventType.KeyDown", "KeyEventType.KeyUp",
                      "repeatCount", "isCanceled", "activation.reset()"):
            self.assertIn(value, source)
        self.assertIn("rememberUpdatedState(onActivate)", source)

    def test_single_and_empty_lists_remain_noninteractive(self):
        source = (DETAIL / "TrackSection.kt").read_text()
        self.assertIn("if (options.isEmpty()) return", source)
        self.assertIn("if (options.size > 1)", source)
        self.assertIn('if (option == SUBTITLE_OFF_SENTINEL) "Desactivado"', source)

    def test_saved_tracks_and_version_callbacks_are_retained(self):
        detail = (DETAIL / "DetailContent.kt").read_text()
        for value in ("setPreferredAudioStreamIndex", "setPreferredSubtitleStreamIndex",
                      "onPreferredStreamIndexesChanged", "onVersionItemSelected(selectedVersionId)"):
            self.assertIn(value, detail)
        source = (DETAIL / "TrackSection.kt").read_text()
        self.assertIn('private const val SUBTITLE_OFF_SENTINEL = "Off"', source)
        self.assertIn("if (selectedOption == SUBTITLE_OFF_SENTINEL) return -1", source)


if __name__ == "__main__":
    unittest.main(verbosity=2)
