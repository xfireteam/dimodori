"""Source contracts supplement the runnable policy and native rendering test.

These do NOT claim that Android compilation or visual validation has passed.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
TV = ROOT / "phone/src/main/java/com/dimodori/app/tv/ui"
SHARED = ROOT / "shared/src/androidMain/com/jellycine/shared/ui/components/common/SimilarItems.kt"


class TvOutlineContract(unittest.TestCase):
    def test_decoration_cannot_add_layout_or_input_targets(self):
        source = (TV / "focus/TvContentFocusOutline.kt").read_text()
        for forbidden in (".clickable(", ".focusable(", ".focusTarget(", ".padding(",
                          ".onKeyEvent(", ".onPreviewKeyEvent(", ".size("):
            self.assertNotIn(forbidden, source)
        self.assertIn("drawWithCache", source)
        self.assertIn("2.dp.toPx()", source)
        self.assertIn("Color.White", source)
        self.assertLess(source.index("drawContent()"), source.index("drawOutline(outline"))
        self.assertIn("it.hasFocus", source)
        self.assertIn("remember { TvContentFocus() }", source)

    def test_tv_card_renderers_observe_focus_and_outline_typed_content(self):
        expected = {
            "dashboard/home/Dashboard.kt": 2,
            "dashboard/media/ViewAllScreen.kt": 2,
            "dashboard/search/SearchResults.kt": 3,
            "dashboard/favorites/Favorites.kt": 1,
            "dashboard/media/Awards.kt": 1,
            "detail/BoxSetSection.kt": 1,
            "detail/DetailScreen.kt": 1,
            "detail/EpisodeCard.kt": 1,
            "detail/PersonScreen.kt": 1,
            "detail/PreviewComponents.kt": 1,
        }
        for path, count in expected.items():
            with self.subTest(path=path):
                source = (TV / "screens" / path).read_text()
                self.assertEqual(source.count(".tvContentFocusObserver("), count)
                self.assertEqual(len(re.findall(
                    r"\.tvContentFocusOutline\(\s*(?:item|episode)\.(?:type|mediaType),",
                    source)), count)

    def test_shared_recommendations_require_explicit_opt_in(self):
        source = SHARED.read_text()
        self.assertIn("imageFocusModifier: ((String?, Boolean, Shape) -> Modifier)? = null", source)
        self.assertEqual(source.count("if (imageFocusModifier != null)"), 2)
        self.assertEqual(source.count(") ?: Modifier)"), 2)
        self.assertNotIn("com.dimodori.app.tv", source)
        callers = []
        for path in (ROOT / "phone/src/main").rglob("*.kt"):
            if "imageFocusModifier =" in path.read_text():
                callers.append(path.relative_to(ROOT).as_posix())
        self.assertEqual(callers, [
            "phone/src/main/java/com/dimodori/app/tv/ui/screens/detail/Recommendations.kt"
        ])


if __name__ == "__main__":
    unittest.main()
