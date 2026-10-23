#!/usr/bin/env python3
"""Run actual Kotlin policy, then source/XML wiring checks (not Android UI execution)."""
from pathlib import Path
import subprocess
import tempfile
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "phone/src/main/java/com/dimodori/app"
SEARCH = APP / "tv/ui/screens/dashboard/search"
ANDROID = "{http://schemas.android.com/apk/res/android}"


class VoiceSearchContract(unittest.TestCase):
    def test_matching_controls_and_predictable_horizontal_focus(self):
        source = (SEARCH / "SearchActions.kt").read_text()
        self.assertEqual(source.count("SearchActionButton("), 3)  # Two callers + definition.
        self.assertEqual(source.count(".size(48.dp)"), 1)
        self.assertEqual(source.count(".size(22.dp)"), 1)
        self.assertIn("CircleShape", source)
        self.assertIn("right = voiceFocusRequester", source)
        self.assertIn("left = searchFocusRequester", source)
        self.assertIn("stringResource(R.string.tv_voice_search)", source)
        self.assertIn(".trackSelectorActivation(onClick)", source)
        self.assertNotIn(".focusable(", source)  # clickable owns the sole focus target.
        self.assertNotIn("tvContentFocusOutline", source)

    def test_mic_remains_available_while_editing_and_after_dictation(self):
        source = (SEARCH / "SearchContainer.kt").read_text()
        self.assertEqual(source.count("SearchActions("), 2)
        self.assertEqual(source.count("onVoice = startVoiceSearch"), 2)
        self.assertIn("viewModel.updateSearchQuery(query)", source)
        self.assertIn("onReturned = { restoreVoiceFocus = true }", source)
        self.assertIn("lifecycleOwner.lifecycle.withResumed", source)
        self.assertLess(source.index("withFrameNanos"), source.index("voiceFocusRequester.requestFocus()"))
        self.assertIn("onLetterClick = { viewModel.updateSearchQuery(searchQuery + it) }", source)
        self.assertIn("if (isKeyboardOpen) keyboardFocusRequester.requestFocus()", source)
        self.assertIn("SearchTypeChips(", source)

    def test_recognizer_is_explicit_and_errors_are_contained(self):
        source = (SEARCH / "VoiceSearch.kt").read_text()
        for expected in ("ActivityResultContracts.StartActivityForResult()",
                         "RecognizerIntent.ACTION_RECOGNIZE_SPEECH",
                         "RecognizerIntent.LANGUAGE_MODEL_FREE_FORM",
                         "RecognizerIntent.EXTRA_RESULTS",
                         "RecognizerIntent.EXTRA_LANGUAGE, locale",
                         "LocalConfiguration.current.locales[0].toLanguageTag()",
                         "request.begin(currentScope())",
                         "is VoiceSearchOutcome.Query -> currentOnQuery(outcome.text)",
                         "catch (_: ActivityNotFoundException)",
                         "catch (_: RuntimeException)",
                         "onDispose { request.invalidate() }",
                         "Lifecycle.State.STARTED", "Lifecycle.State.RESUMED"):
            self.assertIn(expected, source)
        self.assertEqual(source.count("launcher.launch("), 1)
        self.assertNotIn("SpeechRecognizer.create", source)
        self.assertNotIn("RECORD_AUDIO", source)

    def test_no_recording_permissions_or_required_microphone(self):
        manifest = ET.parse(ROOT / "phone/src/main/AndroidManifest.xml").getroot()
        permissions = {node.get(ANDROID + "name") for node in manifest.findall("uses-permission")}
        self.assertNotIn("android.permission.RECORD_AUDIO", permissions)
        for node in manifest.findall("uses-feature"):
            if node.get(ANDROID + "name") == "android.hardware.microphone":
                self.assertEqual(node.get(ANDROID + "required"), "false")

    def test_localized_messages_are_complete_and_unique(self):
        names = ("tv_voice_search", "tv_voice_search_prompt", "tv_voice_search_empty",
                 "tv_voice_search_failed", "tv_voice_search_unavailable")
        for locale in ("values", "values-es"):
            root = ET.parse(ROOT / f"shared/src/androidMain/res/{locale}/strings.xml").getroot()
            for name in names:
                matching = [node for node in root.findall("string") if node.get("name") == name]
                self.assertEqual(len(matching), 1, f"{locale}: {name}")
                self.assertTrue(matching[0].text)


if __name__ == "__main__":
    with tempfile.TemporaryDirectory(prefix="dimodori-voice-") as temp:
        jar = str(Path(temp) / "checks.jar")
        subprocess.run([
            "kotlinc", str(SEARCH / "VoiceSearchRequest.kt"),
            str(APP / "tv/ui/screens/detail/TrackSelectorActivation.kt"),
            str(ROOT / "scripts/VoiceSearchChecks.kt"),
            "-include-runtime", "-d", jar,
        ], check=True)
        subprocess.run(["java", "-jar", jar], check=True)
    unittest.main()
