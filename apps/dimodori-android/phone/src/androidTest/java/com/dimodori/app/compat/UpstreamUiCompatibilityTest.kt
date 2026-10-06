package com.dimodori.app.compat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dimodori.app.ui.navigation.ScreenBackHandler
import com.dimodori.app.ui.playerpip.PipTestActivity
import com.dimodori.app.ui.screens.dashboard.DashboardHomeBackHandler
import com.dimodori.app.ui.screens.dashboard.settings.SeerrConnectionStatus
import com.dimodori.app.ui.screens.dashboard.settings.SeerrStatusChip
import com.dimodori.app.ui.screens.player.setDesiredHdrHeadroomCompat
import com.jellycine.shared.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Native callback/touch tests on a debug fixture, not actual video playback. */
@RunWith(AndroidJUnit4::class)
class UpstreamUiCompatibilityTest {
    @get:Rule val compose = createAndroidComposeRule<PipTestActivity>()

    @Test
    fun enteringAndRestoringHeadroomDoesNotThrow() {
        compose.setContent { Row {} }
        compose.runOnIdle {
            val window = compose.activity.window
            val originalColorMode = window.colorMode
            setDesiredHdrHeadroomCompat(window, 4f)
            setDesiredHdrHeadroomCompat(window, 1f)
            assertEquals(originalColorMode, window.colorMode)
        }
    }

    @Test
    fun screenBackDefersToChildAndThenPopsTheScreen() {
        var showChild by mutableStateOf(true)
        var childBacks = 0
        var screenBacks = 0
        var fallbackBacks = 0
        compose.setContent {
            BackHandler { fallbackBacks++ }
            ScreenBackHandler { screenBacks++ }
            if (showChild) {
                BackHandler {
                    childBacks++
                    showChild = false
                }
            }
        }
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle {
            assertEquals(1, childBacks)
            assertEquals(0, screenBacks)
            compose.activity.onBackPressedDispatcher.onBackPressed()
        }
        compose.runOnIdle {
            assertEquals(1, screenBacks)
            assertEquals(0, fallbackBacks)
        }
    }

    @Test
    fun pausedScreenCannotInterceptBack() {
        val owner = TestLifecycleOwner()
        var screenBacks = 0
        var fallbackBacks = 0
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.setContent {
            BackHandler { fallbackBacks++ }
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                ScreenBackHandler { screenBacks++ }
            }
        }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle {
            assertEquals(0, screenBacks)
            assertEquals(1, fallbackBacks)
        }
    }

    @Test
    fun dashboardBackOverridesInnerTabHistoryAndReturnsHome() {
        lateinit var navigation: NavHostController
        compose.setContent {
            navigation = rememberNavController()
            val entry by navigation.currentBackStackEntryAsState()
            NavHost(navigation, startDestination = "home") {
                composable("home") {}
                composable("favorites") {}
                composable("settings") {}
            }
            DashboardHomeBackHandler(entry?.destination?.route, "home", false) {
                navigation.navigate("home") {
                    popUpTo(navigation.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        compose.runOnIdle {
            navigation.navigate("favorites")
            navigation.navigate("settings")
        }
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle { assertEquals("home", navigation.currentDestination?.route) }
    }

    @Test
    fun tapsOnEveryStatusLabelReachTheConnectionCard() {
        var status by mutableStateOf(SeerrConnectionStatus.CONNECTED)
        var taps = 0
        compose.setContent {
            MaterialTheme {
                Row(Modifier.clickable { taps++ }) { SeerrStatusChip(status) }
            }
        }
        val cases = listOf(
            SeerrConnectionStatus.CONNECTED to R.string.settings_seerr_status_connected,
            SeerrConnectionStatus.CHECKING to R.string.settings_seerr_status_checking,
            SeerrConnectionStatus.CONNECTING to R.string.settings_seerr_status_checking,
            SeerrConnectionStatus.ERROR to R.string.settings_seerr_status_issue,
            SeerrConnectionStatus.DISCONNECTED to R.string.settings_seerr_status_not_connected,
        )
        cases.forEachIndexed { index, (state, label) ->
            compose.runOnIdle { status = state }
            compose.onNodeWithText(compose.activity.getString(label), useUnmergedTree = true)
                .performTouchInput { click() }
            compose.runOnIdle { assertEquals(index + 1, taps) }
        }
    }

    private class TestLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
