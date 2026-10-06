package com.dimodori.app.compat

import com.dimodori.app.ui.screens.dashboard.shouldReturnDashboardHome
import com.dimodori.app.ui.screens.player.supportsWindowHdrHeadroom
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpstreamPolicyTest {
    @Test
    fun hdrHeadroomRequiresAndroid15() {
        assertFalse(supportsWindowHdrHeadroom(27))
        assertFalse(supportsWindowHdrHeadroom(34))
        assertTrue(supportsWindowHdrHeadroom(35))
        assertTrue(supportsWindowHdrHeadroom(37))
    }

    @Test
    fun otherTabsReturnHomeButHomeAndUninitializedNavigationDoNot() {
        assertTrue(shouldReturnDashboardHome("settings", "home", true, false))
        assertTrue(shouldReturnDashboardHome("search", "home", true, false))
        assertFalse(shouldReturnDashboardHome("home", "home", true, false))
        assertFalse(shouldReturnDashboardHome(null, "home", true, false))
    }

    @Test
    fun overlaysAndPausedScreensKeepPriority() {
        assertFalse(shouldReturnDashboardHome("settings", "home", true, true))
        assertFalse(shouldReturnDashboardHome("settings", "home", false, false))
        assertFalse(shouldReturnDashboardHome("settings", "home", false, true))
    }
}
