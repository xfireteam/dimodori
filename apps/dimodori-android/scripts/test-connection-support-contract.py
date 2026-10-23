#!/usr/bin/env python3
"""Source/resource checks only; these do not launch an Android app or WhatsApp."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
AUTH = ROOT / "phone/src/main/java/com/dimodori/app/ui/screens/auth"


class ConnectionSupportContract(unittest.TestCase):
    def test_link_is_unconditional_immediately_after_connect_button(self):
        source = (AUTH / "AuthScreen.kt").read_text()
        form = source.split("private fun ConnectionForm(", 1)[1].split("private fun LoginForm(", 1)[0]
        self.assertEqual(source.count("ConnectionSupportLink()"), 1)
        self.assertIn(
            "Text(stringResource(R.string.auth_connect_to_server))\n"
            "                }\n            }\n            ConnectionSupportLink()", form
        )
        self.assertIn("enabled = !isLoading && serverUrl.isNotBlank()", form)
        # The existing content still scrolls instead of positioning support over the CTA.
        self.assertIn("modifier = modifier.verticalScroll(rememberScrollState())", source)

    def test_https_can_open_whatsapp_or_a_browser_without_preflight(self):
        source = (AUTH / "ConnectionSupportLink.kt").read_text()
        self.assertIn('ConnectionSupportUrl = "https://wa.me/17869365291"', source)
        self.assertIn("Intent(Intent.ACTION_VIEW, Uri.parse(ConnectionSupportUrl))", source)
        self.assertNotIn("setPackage", source)
        self.assertNotIn("resolveActivity", source)
        self.assertNotIn("whatsapp://", source)
        self.assertNotIn("onConnect", source)

    def test_accessible_secondary_control_is_always_enabled(self):
        source = (AUTH / "ConnectionSupportLink.kt").read_text()
        for expected in ("TextButton(", ".fillMaxWidth().heightIn(min = 48.dp)",
                         "TextDecoration.Underline", "MaterialTheme.typography.bodyMedium",
                         "ButtonDefaults.textButtonColors(contentColor = JellyBlue)"):
            self.assertIn(expected, source)
        self.assertNotIn("enabled =", source)

    def test_missing_handler_or_blocked_launch_provides_feedback(self):
        source = (AUTH / "ConnectionSupportLink.kt").read_text()
        self.assertIn("catch (_: ActivityNotFoundException)", source)
        self.assertIn("catch (_: SecurityException)", source)
        self.assertEqual(source.count("R.string.auth_support_open_failed"), 2)
        self.assertNotIn("AuthRepository", source)

    def test_resources_have_exact_spanish_label_and_clear_errors(self):
        for locale, label in (("values", "Support?"), ("values-es", "¿Soporte?")):
            root = ET.parse(ROOT / f"shared/src/androidMain/res/{locale}/strings.xml").getroot()
            for name in ("auth_support_link", "auth_support_open_failed"):
                nodes = [node for node in root.findall("string") if node.get("name") == name]
                self.assertEqual(len(nodes), 1)
                self.assertTrue(nodes[0].text)
                if name == "auth_support_link":
                    self.assertEqual(nodes[0].text, label)


if __name__ == "__main__":
    unittest.main()
