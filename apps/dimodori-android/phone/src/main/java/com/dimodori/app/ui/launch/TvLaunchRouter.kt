package com.dimodori.app.ui.launch

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.media3.common.util.UnstableApi
import com.dimodori.app.tv.ui.activity.DimodoriTvActivity

/**
 * The generic launcher (including Play's Open button) can target the touch entry
 * on a TV. Choose the UI by device capability, not by the launcher category,
 * screen size, orientation, or the presence of a controller.
 */
@UnstableApi
internal object TvLaunchRouter {
    fun isTelevision(context: Context): Boolean =
        isTelevision { feature -> context.packageManager.hasSystemFeature(feature) }

    internal fun isTelevision(hasSystemFeature: (String) -> Boolean): Boolean =
        hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    fun createTvIntent(context: Context, sourceIntent: Intent): Intent =
        Intent(sourceIntent).apply {
            setClass(context, DimodoriTvActivity::class.java)
            removeCategory(Intent.CATEGORY_LAUNCHER)
            if (action == Intent.ACTION_MAIN) {
                addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            }

            // Stay in the task already chosen by the caller. Do not forward
            // NEW_TASK/CLEAR_TASK/MULTIPLE_TASK/NEW_DOCUMENT from an installer.
            // CLEAR_TOP + SINGLE_TOP reuses an existing TV activity (and its
            // Compose navigation) instead of stacking a second one on reopen.
            // Preserve payload and URI permissions by copying the intent.
            flags = (sourceIntent.flags and URI_GRANT_FLAGS) or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

    private const val URI_GRANT_FLAGS =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
}
