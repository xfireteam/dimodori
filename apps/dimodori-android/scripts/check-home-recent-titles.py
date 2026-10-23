"""Execute the actual repository methods with SDK-free Kotlin doubles.

Also check UI wiring contracts; these are not Android rendering/input tests.
Requires kotlinc and java, and writes generated files only in a temporary dir.
"""
from pathlib import Path
import subprocess
import tempfile
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "phone/src/main/java/com/dimodori/app"
REPO = ROOT / "data/src/androidMain/com/jellycine/data/repository/MediaRepository.kt"
MOBILE = APP / "ui/screens/dashboard/home/Dashboard.kt"
TV = APP / "tv/ui/screens/dashboard/home/Dashboard.kt"


def run_repository_checks():
    source = REPO.read_text()
    methods = source[source.index("    suspend fun getRecentlyAddedMovies("):
                     source.index("    suspend fun getRecentlyAddedEpisodes(")]
    with tempfile.TemporaryDirectory(prefix="dimodori-recent-") as temp:
        folder = Path(temp)
        harness = folder / "RecentTitlesHarness.kt"
        harness.write_text(
            "import java.util.concurrent.CancellationException\n"
            "class RecentTitlesHarness(private val session: Session?) {\n"
            "private fun getApiSession() = session\n"
            'private fun string(id: Int, vararg args: Any) = "resource-$id"\n'
            + methods + "\n}\n"
        )
        jar = folder / "checks.jar"
        subprocess.run(["kotlinc", str(harness), str(ROOT / "scripts/RecentHomeTitlesChecks.kt"),
                        "-include-runtime", "-d", str(jar)], check=True)
        subprocess.run(["java", "-jar", str(jar)], check=True)


class RecentHomeWiring(unittest.TestCase):
    def test_order_in_both_interfaces(self):
        for path in (MOBILE, TV):
            with self.subTest(path=path):
                source = path.read_text()
                start = source.index('item(key = "continue_watching_section")')
                series = source.index("item(key = recentSeriesKey)", start)
                movies = source.index("item(key = recentMoviesKey)", series)
                next_up = source.index('item(key = "next_up_section")', movies)
                libraries = source.index('item(key = "home_libraries_top_padding")', next_up)
                self.assertLess(start, series)
                self.assertLess(series, movies)
                self.assertLess(movies, next_up)
                self.assertLess(next_up, libraries)
                self.assertNotIn("item(key =", source[start:series].split("if (selectedCategory")[0][
                    len('item(key = "continue_watching_section")'):])
                # No Seerr section or other lazy item between the two new rows.
                self.assertNotIn("item(key =", source[series:movies][len("item(key = recentSeriesKey)"):])

    def test_queries_are_independent_home_online_and_session_scoped(self):
        for path in (MOBILE, TV):
            with self.subTest(path=path):
                source = path.read_text()
                block = source[source.index("val recentSeriesKey"):source.index("val nextUpQuery")]
                self.assertIn("sessionSnapshot.activeServerId", block)
                self.assertIn("selectedCategory == HomeCategory.HOME", block)
                self.assertIn("isNetworkAvailable", block)
                self.assertEqual(block.count("enabled = recentTitlesEnabled"), 2)
                self.assertIn("getRecentlyAddedSeries(limit = 24)", block)
                self.assertIn("getRecentlyAddedMovies(limit = 24)", block)
                self.assertEqual(block.count("currentCoroutineContext().ensureActive()"), 2)
                self.assertNotIn("Seerr", block)
                self.assertIn("remember(dashboardSessionKey)", source)
                self.assertIn("remember(key, queryManager)", source)
                self.assertEqual(source.count("invalidateQuery(recentSeriesKey)"), 2)
                self.assertEqual(source.count("invalidateQuery(recentMoviesKey)"), 2)
                self.assertIn("if (isNetworkAvailable) {", source)

    def test_empty_error_and_stale_content_are_handled_separately(self):
        source = (APP / "ui/components/common/RecentHomeSection.kt").read_text()
        self.assertIn("items.isEmpty() && !isLoading && !isError", source)
        self.assertLess(source.index("items.isNotEmpty() ->"), source.index("isLoading ->"))
        self.assertIn("isError -> Text(", source)
        self.assertIn("itemsIndexed(items, key =", source)
        self.assertIn("LibrarySkeleton()", source)

    def test_tv_reuses_cards_and_keeps_single_content_entry(self):
        source = TV.read_text()
        wrapper = (APP / "tv/ui/screens/dashboard/home/RecentTitlesSection.kt").read_text()
        self.assertIn("LibraryItemCard(", wrapper)
        self.assertIn("entryCardModifier(", wrapper)
        self.assertIn("Modifier.focusGroup()", wrapper)
        self.assertIn("if (index == 0) initialFocusRequester", wrapper)
        self.assertIn("onReturnTargetChanged = onReturnTargetChanged", wrapper)
        self.assertIn("!hasRecentSeriesCards", source)
        self.assertEqual(source.count("!recentTitlesOwnEntry"), 5)
        self.assertIn(".tvContentFocusOutline(item.type,", source)
        self.assertNotIn("tvContentFocusOutline", MOBILE.read_text())

    def test_shared_translatable_titles(self):
        for locale, titles in {
            "values": ("Recently Added Series", "Recently Added Movies"),
            "values-es": ("Últimas series agregadas", "Últimas películas agregadas")
        }.items():
            root = ET.parse(ROOT / f"shared/src/androidMain/res/{locale}/strings.xml").getroot()
            strings = {node.attrib.get("name"): node.text for node in root.findall("string")}
            self.assertEqual(strings["dashboard_recent_series"], titles[0])
            self.assertEqual(strings["dashboard_recent_movies"], titles[1])
            self.assertTrue(strings["dashboard_failed_recent_titles"])

    def test_both_server_clients_share_the_user_scoped_route(self):
        source = (ROOT / "data/src/commonMain/com/jellycine/data/api/MediaServerApiClient.kt").read_text()
        start = source.index("override suspend fun getUserItems(")
        block = source[start:source.index("override suspend fun getSuggestions(", start)]
        self.assertIn('endpoint = "Users/$userId/Items"', block)
        for param in ("includeItemTypes", "recursive", "sortBy", "sortOrder", "limit"):
            self.assertIn(f'"{param}" to {param}', block)


if __name__ == "__main__":
    run_repository_checks()
    unittest.main()
