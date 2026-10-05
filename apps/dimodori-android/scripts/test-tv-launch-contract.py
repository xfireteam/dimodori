#!/usr/bin/env python3
"""SDK-free source-contract checks, NOT Android compilation or runtime tests."""

from pathlib import Path
import unittest
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "phone/src/main/java/com/dimodori/app"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def function_body(source: str, name: str) -> str:
    """Extract the braced functions used by these narrowly scoped checks."""
    start = source.index(f"fun {name}(")
    opening = source.index("{", start)
    depth = 1
    for index in range(opening + 1, len(source)):
        depth += (source[index] == "{") - (source[index] == "}")
        if depth == 0:
            return source[opening + 1:index]
    raise AssertionError(f"Unclosed function: {name}")


class TvLaunchContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.phone = (JAVA / "ui/activity/DimodoriActivity.kt").read_text()
        cls.tv = (JAVA / "tv/ui/activity/DimodoriActivity.kt").read_text()
        cls.router = (JAVA / "ui/launch/TvLaunchRouter.kt").read_text()
        cls.manifest = ET.parse(ROOT / "phone/src/main/AndroidManifest.xml").getroot()

    def test_route_precedes_mobile_effects_and_composition(self):
        body = function_body(self.phone, "onCreate")
        guard = body.index("if (TvLaunchRouter.isTelevision(this))")
        branch = body[guard:body.index("AppLanguageManager.applySavedLanguage")]
        self.assertLess(body.index("super.onCreate(savedInstanceState)"), guard)
        for effect in (
            "requestNotificationPermission()",
            "AuthStateManager.getInstance",
            "lifecycleScope.launch",
            "enableEdgeToEdge()",
            "setContent",
        ):
            self.assertLess(guard, body.index(effect))
        self.assertIn("splashScreen.setKeepOnScreenCondition { true }", branch)
        self.assertIn("openTv(intent)", branch)
        self.assertIn("return", branch)
        # The TV decision is not restricted to the first creation.
        self.assertNotIn("savedInstanceState", branch)

    def test_redirect_finishes_mobile_entry_after_opening_tv(self):
        body = function_body(self.phone, "openTv")
        self.assertIn("TvLaunchRouter.createTvIntent(this, sourceIntent)", body)
        self.assertLess(body.index("startActivity("), body.index("finish()"))

    def test_reused_entries_retain_latest_intent(self):
        phone_body = function_body(self.phone, "onNewIntent")
        self.assertIn("super.onNewIntent(intent)", phone_body)
        self.assertIn("setIntent(intent)", phone_body)
        self.assertIn("if (TvLaunchRouter.isTelevision(this))", phone_body)
        self.assertIn("openTv(intent)", phone_body)
        self.assertIn("setIntent(intent)", function_body(self.tv, "onNewIntent"))
        self.assertNotIn("TvLaunchRouter", self.tv)  # No reverse routing loop.

    def test_routing_uses_only_real_tv_capability(self):
        self.assertIn(
            "hasSystemFeature(PackageManager.FEATURE_LEANBACK)", self.router
        )
        code = "\n".join(
            line for line in self.router.splitlines()
            if not line.strip().startswith(("//", "*", "/**"))
        )
        for heuristic in ("screenSize", "orientation", "FEATURE_GAMEPAD", "uiMode"):
            self.assertNotIn(heuristic, code)

    def test_intent_is_copied_and_reuses_tv_without_task_reset(self):
        self.assertIn("Intent(sourceIntent).apply", self.router)
        self.assertIn("setClass(context, DimodoriTvActivity::class.java)", self.router)
        self.assertIn("removeCategory(Intent.CATEGORY_LAUNCHER)", self.router)
        self.assertIn("addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)", self.router)
        flags = self.router[self.router.index("flags ="):self.router.index("private const")]
        self.assertIn("sourceIntent.flags and URI_GRANT_FLAGS", flags)
        self.assertIn("FLAG_ACTIVITY_CLEAR_TOP", flags)
        self.assertIn("FLAG_ACTIVITY_SINGLE_TOP", flags)
        for forbidden in ("NEW_TASK", "CLEAR_TASK", "MULTIPLE_TASK", "NEW_DOCUMENT"):
            self.assertNotIn(forbidden, flags)
        for grant in ("READ", "WRITE", "PERSISTABLE", "PREFIX"):
            self.assertIn(f"FLAG_GRANT_{grant}_URI_PERMISSION", self.router)

    def test_shared_installer_retains_both_launcher_entries(self):
        features = {
            feature.attrib[ANDROID + "name"]: feature.attrib[ANDROID + "required"]
            for feature in self.manifest.findall("uses-feature")
        }
        self.assertEqual("false", features["android.software.leanback"])
        self.assertEqual("false", features["android.hardware.touchscreen"])
        activities = {
            activity.attrib[ANDROID + "name"]: activity
            for activity in self.manifest.findall("application/activity")
        }
        for name, category in (
            (".ui.activity.DimodoriActivity", "android.intent.category.LAUNCHER"),
            (".tv.ui.activity.DimodoriTvActivity", "android.intent.category.LEANBACK_LAUNCHER"),
        ):
            activity = activities[name]
            self.assertEqual("true", activity.attrib[ANDROID + "exported"])
            self.assertEqual("singleTop", activity.attrib[ANDROID + "launchMode"])
            self.assertIn(
                category,
                [item.attrib[ANDROID + "name"] for item in activity.findall("intent-filter/category")],
            )
            self.assertIn(
                "android.intent.action.MAIN",
                [item.attrib[ANDROID + "name"] for item in activity.findall("intent-filter/action")],
            )
        self.assertIn(
            'applicationId = "com.dimodori.app"',
            (ROOT / "phone/build.gradle").read_text(),
        )


if __name__ == "__main__":
    unittest.main(verbosity=2)
