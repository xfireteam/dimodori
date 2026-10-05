package com.dimodori.app.ui.launch

import android.content.pm.PackageManager
import androidx.media3.common.util.UnstableApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@UnstableApi
class TvLaunchRouterTest {
    @Test
    fun leanbackDeviceUsesTvEvenWhenOpenedByGenericLauncher() {
        assertTrue(TvLaunchRouter.isTelevision { it == PackageManager.FEATURE_LEANBACK })
    }

    @Test
    fun phoneWithoutLeanbackKeepsTouchUi() {
        assertFalse(TvLaunchRouter.isTelevision { it == PackageManager.FEATURE_TOUCHSCREEN })
    }

    @Test
    fun tabletWithControllerDoesNotBecomeTv() {
        val features = setOf(
            PackageManager.FEATURE_TOUCHSCREEN,
            PackageManager.FEATURE_GAMEPAD,
        )
        assertFalse(TvLaunchRouter.isTelevision { it in features })
    }

    @Test
    fun noTvCapabilityKeepsTouchUi() {
        assertFalse(TvLaunchRouter.isTelevision { false })
    }

    @Test
    fun checksDeviceCapabilityRatherThanLaunchSourceOrScreenConfiguration() {
        val queried = mutableListOf<String>()
        TvLaunchRouter.isTelevision {
            queried += it
            true
        }
        assertEquals(listOf(PackageManager.FEATURE_LEANBACK), queried)
    }
}
