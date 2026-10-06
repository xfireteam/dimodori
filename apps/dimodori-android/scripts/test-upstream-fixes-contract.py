#!/usr/bin/env python3
"""SDK-free wiring checks. They do NOT execute Android, R8, or touch events."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "phone/src/main/java/com/dimodori/app"


class UpstreamFixesContract(unittest.TestCase):
    def read(self, name):
        return (JAVA / name).read_text()

    def test_hdr_has_no_direct_api35_call_and_guards_reflection(self):
        for file in JAVA.rglob("*.kt"):
            self.assertNotRegex(file.read_text(), r"\.\s*setDesiredHdrHeadroom\s*\(")
        compat = self.read("ui/screens/player/WindowHdrCompat.kt")
        self.assertLess(
            compat.index("if (!supportsWindowHdrHeadroom(Build.VERSION.SDK_INT)) return"),
            compat.index('.getMethod("setDesiredHdrHeadroom"'),
        )
        self.assertIn("runCatching", compat)
        policy = self.read("ui/screens/player/HdrHeadroomPolicy.kt")
        self.assertIn("sdk >= 35", policy)

    def test_hdr_cleanup_preserves_the_existing_color_restore_and_pip_action(self):
        effects = self.read("ui/screens/player/PlayerScreenSections.kt")
        self.assertIn("setDesiredHdrHeadroomCompat(activity.window, 4.0f)", effects)
        self.assertIn("activity.window.colorMode = originalColorMode", effects)
        self.assertIn("setDesiredHdrHeadroomCompat(activity.window, 1.0f)", effects)
        self.assertIn("onEnterPip = onEnterPip", effects)

    def test_all_settings_routes_register_before_child_content(self):
        navigation = self.read("ui/navigation/AppNavigation.kt")
        for screen in (
            "PlayerSettingsScreen", "SubtitleSettingsScreen", "InterfaceSettingsScreen",
            "ConnectionsSettingsScreen", "CacheSettingsScreen", "AboutScreen",
            "ServerInfoScreen", "ScreenTimeScreen",
        ):
            self.assertRegex(
                navigation,
                r"ScreenBackHandler \{ navController\.popBackStack\(\) \}\s+" + screen + r"\(",
            )
        self.assertIn("textTransition(450)", navigation)
        self.assertIn("textExitTransition(350)", navigation)

    def test_back_handler_only_intercepts_resumed_screen(self):
        helper = self.read("ui/navigation/ScreenBackHandler.kt")
        self.assertIn("enabled && state == Lifecycle.State.RESUMED", helper)
        self.assertIn("onBack = onBack", helper)

    def test_view_all_defers_to_sort_sheet(self):
        screen = self.read("ui/screens/dashboard/media/ViewAllScreen.kt")
        self.assertIn("ScreenBackHandler(enabled = !showSortSheet, onBack = onBackPressed)", screen)
        self.assertIn("onDismiss = { showSortSheet = false }", screen)

    def test_dashboard_handler_overrides_tab_history_but_defers_to_overlays(self):
        dashboard = self.read("ui/screens/dashboard/DashboardContainer.kt")
        handler = dashboard.index("DashboardHomeBackHandler(")
        self.assertLess(dashboard.index("NavHost("), handler)
        self.assertLess(handler, dashboard.index("// Curved Bottom Navigation"))
        self.assertIn("hasOverlay = showAccountSheet || showUserSwitchDialog", dashboard)
        self.assertIn("onHome = { navigateToDestination(DashboardDestination.Home) }", dashboard)
        helper = self.read("ui/screens/dashboard/DashboardHomeBackHandler.kt")
        self.assertIn("state == Lifecycle.State.RESUMED", helper)
        self.assertIn("shouldReturnDashboardHome(", helper)

    def test_status_label_has_no_interactive_chip(self):
        source = self.read("ui/screens/dashboard/settings/SeerrDialog.kt")
        start = source.index("internal fun SeerrStatusChip(")
        end = source.index("private fun SeerrStatusHeader", start)
        badge = source[start:end]
        self.assertIn("Surface(", badge)
        self.assertIn("text = label", badge)
        self.assertIn("color = containerColor", badge)
        self.assertNotIn("AssistChip(", badge)
        self.assertNotIn("onClick =", badge)
        self.assertNotIn(".clickable", badge)

    def test_downloads_still_owns_its_back_and_embedded_player_layers(self):
        downloads = self.read("ui/screens/dashboard/settings/DownloadsScreen.kt")
        self.assertIn("onBack = closeCurrentLayer", downloads)
        self.assertIn("PlayerScreen(", downloads)
        self.assertIn("selectedSeries != null", downloads)


if __name__ == "__main__":
    unittest.main(verbosity=2)
