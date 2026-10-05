#!/usr/bin/env python3
"""SDK-free wiring checks; these do not execute the Android player/window."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "phone/src/main/java/com/dimodori/app"
A = "{http://schemas.android.com/apk/res/android}"


class PipContractTests(unittest.TestCase):
    def read(self, path):
        return (JAVA / path).read_text()

    def test_only_mobile_declares_pip_and_handles_resize(self):
        manifest = ET.parse(ROOT / "phone/src/main/AndroidManifest.xml")
        activities = manifest.findall("application/activity")
        phone, tv = activities[:2]
        self.assertEqual("true", phone.get(A + "supportsPictureInPicture"))
        self.assertIsNone(tv.get(A + "supportsPictureInPicture"))
        config = phone.get(A + "configChanges").split("|")
        for name in ("orientation", "screenSize", "screenLayout", "smallestScreenSize"):
            self.assertIn(name, config)
        self.assertNotIn("android.permission.SYSTEM_ALERT_WINDOW", manifest.getroot().attrib.values())
        permissions = [p.get(A + "name") for p in manifest.findall("uses-permission")]
        self.assertNotIn("android.permission.SYSTEM_ALERT_WINDOW", permissions)

    def test_both_engines_keep_surface_and_skip_pip_pause(self):
        exo = self.read("ui/screens/player/VideoSurface.kt")
        mpv = self.read("ui/screens/player/MpvVideoSurface.kt")
        self.assertIn("if (!keepPlaybackOnPause)", exo)
        self.assertIn("Lifecycle.Event.ON_PAUSE && !keepPlaybackOnPause", mpv)
        self.assertIn("player.resizeSurface(width, height)", mpv)
        self.assertIn("player.detachSurface()", mpv)
        self.assertNotIn("release()", mpv)
        self.assertIn("keepPlaybackOnPause = keepPlaybackOnPause", exo)

    def test_video_only_mode_hides_overlays_not_player_or_effects(self):
        screen = self.read("ui/screens/player/PlayerScreen.kt")
        guard = screen.index("if (!pipUi)")
        self.assertLess(screen.index("PlayerScreenEffects("), guard)
        self.assertLess(screen.index("VideoSurface("), guard)
        self.assertGreater(screen.index("PlayerOverlayHost("), guard)
        self.assertGreater(screen.index("PlayerDialogsHost("), guard)
        self.assertIn("remoteMediaUrl.isNullOrBlank()", screen)
        self.assertIn("videoAspect != null", screen)
        self.assertIn("playerState.hasStartedPlayback", screen)
        self.assertIn("BackHandler(enabled = !pipUi)", screen)

    def test_manual_button_and_localized_labels_exist(self):
        controls = self.read("ui/screens/player/ControlsOverlay.kt")
        self.assertIn("IconButton(onClick = onEnterPip)", controls)
        self.assertIn("stringResource(AppR.string.pip_enter)", controls)
        for locale in ("values", "values-es"):
            strings = ET.parse(ROOT / f"phone/src/main/res/{locale}/strings.xml")
            names = {s.get("name") for s in strings.findall("string")}
            self.assertTrue({"pip_enter", "pip_unavailable", "pip_play", "pip_pause"} <= names)

    def test_controller_limits_entry_and_secures_broadcasts(self):
        controller = self.read("ui/playerpip/PlayerPipController.kt")
        for term in (
            "FEATURE_PICTURE_IN_PICTURE", "FEATURE_LEANBACK",
            "OPSTR_PICTURE_IN_PICTURE", "MODE_ALLOWED",
            "Build.VERSION.SDK_INT < 31", "setAutoEnterEnabled",
            "ContextCompat.RECEIVER_NOT_EXPORTED", "PendingIntent.FLAG_IMMUTABLE",
            "setPackage(activity.packageName)", "EXTRA_SESSION",
        ):
            self.assertIn(term, controller)
        self.assertIn("eligible && playing && available", controller)
        self.assertIn("if (binding !== token) return", controller)
        self.assertIn("clearBinding() // Clear first", controller)
        self.assertIn("activity.unregisterReceiver(receiver)", controller)

    def test_activity_delegates_pip_lifecycle_and_retains_tv_routing(self):
        activity = self.read("ui/activity/DimodoriActivity.kt")
        for callback in ("onPause", "onResume", "onStop", "onUserLeaveHint"):
            self.assertIn(f"playerPip.{callback}()", activity)
        self.assertIn("playerPip.onModeChanged(inPip)", activity)
        self.assertIn("playerPip.onUiTransition(pipState.isTransitioningToPip)", activity)
        self.assertIn("TvLaunchRouter.isTelevision(this)", activity)

    def test_final_progress_snapshotted_before_player_release(self):
        reporter = self.read("ui/screens/player/PlayerPlaybackReporter.kt")
        start = reporter.index("fun reportPlaybackStopped(")
        end = reporter.index("fun onPlaybackPauseStateChanged", start)
        body = reporter[start:end]
        self.assertLess(body.index("val finalPositionTicks"), body.index("launch {"))
        self.assertIn("positionTicks = finalPositionTicks", body)
        self.assertIn("withTimeout(15_000L)", body)
        self.assertLess(body.index("hasReportedStart = false"), body.index("launch {"))

    def test_background_pause_and_close_cannot_start_a_delayed_local_load(self):
        model = self.read("ui/screens/player/PlayerViewModel.kt")
        self.assertIn("playerInitializationJob = viewModelScope.launch", model)
        release = model[model.index("fun releasePlayer()"):]
        self.assertIn("playerInitializationJob?.cancel()", release)
        self.assertIn("currentCoroutineContext().ensureActive()", model)
        self.assertIn("startPlayback && _playerState.value.playWhenReady", model)
        self.assertIn("startPlayback = shouldStartPlayback", model)
        self.assertIn("if (e is CancellationException) throw e", model)


if __name__ == "__main__":
    unittest.main(verbosity=2)
