#!/usr/bin/env python3
"""Source wiring checks only; native focus/IME behavior requires Android instrumentation."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
AUTH = ROOT / "phone/src/main/java/com/dimodori/app/tv/ui/screens/auth"


class TvPasswordContract(unittest.TestCase):
    def test_eye_is_a_sibling_not_inside_text_field(self):
        source = (AUTH / "TvPasswordField.kt").read_text()
        self.assertIn("Row(", source)
        self.assertIn("OutlinedTextField(", source)
        self.assertIn("IconButton(", source)
        self.assertNotIn("trailingIcon", source)
        self.assertEqual(source.count("onValueChange = onPasswordChange"), 1)
        self.assertIn("var isPasswordVisible by remember { mutableStateOf(false) }", source)

    def test_remote_can_leave_editor_without_stealing_open_ime_navigation(self):
        source = (AUTH / "TvPasswordField.kt").read_text()
        for expected in ("WindowInsets.isImeVisible", "enabled && !imeVisible",
                         "Key.DirectionRight", "Key.DirectionDown",
                         "navigationPress.keyDown(", "navigationPress.keyUp(",
                         "event.nativeKeyEvent.repeatCount", "event.nativeKeyEvent.isCanceled",
                         "eyeFocus.requestFocus()", "keyboard?.hide()",
                         "left = passwordFocus", "up = passwordFocus", "down = nextFocusRequester"):
            self.assertIn(expected, source)
        self.assertNotIn("Key.Back", source)
        self.assertNotIn("BackHandler", source)

    def test_toggle_consumes_full_presses_and_respects_busy_state(self):
        source = (AUTH / "TvPasswordField.kt").read_text()
        self.assertIn(".trackSelectorActivation {", source)
        self.assertIn("if (enabled) isPasswordVisible = !isPasswordVisible", source)
        self.assertEqual(source.count("enabled = enabled"), 2)
        self.assertIn("Color.White else Color.White.copy(alpha = 0.12f)", source)
        self.assertNotIn("tvContentFocusOutline", source)
        self.assertNotIn(".focusable(", source)
        self.assertIn("KeyboardType.Password", source)
        self.assertIn("R.string.auth_show_password", source)
        self.assertIn("R.string.auth_hide_password", source)
        self.assertNotIn("onLogin", source)
        self.assertNotIn("Log.", source)

    def test_shared_tv_login_form_keeps_login_and_quick_connect_paths(self):
        source = (AUTH / "AuthScreen.kt").read_text()
        form = source.split("internal fun LoginForm(", 1)[1]
        for expected in ("val isBusy = isLoading || isQuickConnectLoading", "TvPasswordField(",
                         "username.isNotBlank() -> signInFocus",
                         "showQuickConnect -> quickConnectFocus",
                         ".focusRequester(signInFocus)", ".focusRequester(quickConnectFocus)",
                         "onClick = onLogin", "onClick = onQuickConnect"):
            self.assertIn(expected, form)
        self.assertIn("startAtLogin: Boolean = false", source)
        self.assertEqual(source.count("LoginForm("), 2)


if __name__ == "__main__":
    unittest.main()
