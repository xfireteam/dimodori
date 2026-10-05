package com.dimodori.app.ui.launch

import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dimodori.app.tv.ui.activity.DimodoriTvActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@UnstableApi
@RunWith(AndroidJUnit4::class)
class TvLaunchIntentTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun genericFirstLaunchTargetsTvWithoutChangingOriginalIntent() {
        val source = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        val original = Intent(source)

        val routed = TvLaunchRouter.createTvIntent(context, source)

        assertEquals(ComponentName(context, DimodoriTvActivity::class.java), routed.component)
        assertEquals(Intent.ACTION_MAIN, routed.action)
        assertTrue(routed.hasCategory(Intent.CATEGORY_LEANBACK_LAUNCHER))
        assertFalse(routed.hasCategory(Intent.CATEGORY_LAUNCHER))
        assertNotSame(source, routed)
        assertTrue(source.filterEquals(original))
        assertEquals(original.flags, source.flags)
    }

    @Test
    fun reopeningReusesTvWithoutForwardingTaskDestructiveFlags() {
        val source = Intent(Intent.ACTION_MAIN).setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        )
        val routed = TvLaunchRouter.createTvIntent(context, source)

        assertEquals(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP,
            routed.flags,
        )
    }

    @Test
    fun forwardingPreservesActionDataTypeExtrasClipDataAndUriPermissions() {
        val uri = Uri.parse("content://test.dimodori/item/42")
        val source = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "video/mp4")
            .addCategory(Intent.CATEGORY_DEFAULT)
            .putExtra("test_source", "installer")
            .putExtra("test_position", 42L)
            .setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        source.clipData = ClipData.newRawUri("test", uri)

        val routed = TvLaunchRouter.createTvIntent(context, source)

        assertEquals(source.action, routed.action)
        assertEquals(source.data, routed.data)
        assertEquals(source.type, routed.type)
        assertEquals("installer", routed.getStringExtra("test_source"))
        assertEquals(42L, routed.getLongExtra("test_position", -1L))
        assertEquals(uri, routed.clipData?.getItemAt(0)?.uri)
        assertTrue(routed.hasCategory(Intent.CATEGORY_DEFAULT))
        assertFalse(routed.hasCategory(Intent.CATEGORY_LEANBACK_LAUNCHER))
        assertEquals(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION,
            routed.flags,
        )
    }

    @Test
    fun directLeanbackLaunchRemainsAnExplicitTvLaunch() {
        val source = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val routed = TvLaunchRouter.createTvIntent(context, source)
        assertEquals(ComponentName(context, DimodoriTvActivity::class.java), routed.component)
        assertTrue(routed.hasCategory(Intent.CATEGORY_LEANBACK_LAUNCHER))
    }

    @Test
    fun runtimeClassificationMatchesDeviceLeanbackCapability() {
        assertEquals(
            context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK),
            TvLaunchRouter.isTelevision(context),
        )
    }
}
