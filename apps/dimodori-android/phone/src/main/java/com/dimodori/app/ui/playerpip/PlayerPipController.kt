package com.dimodori.app.ui.playerpip

import android.app.AppOpsManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.dimodori.app.R

/**
 * Activity-scoped PiP, enabled only while the main local video screen is bound.
 * No player is created here: the same ViewModel/engine owns fullscreen and PiP.
 */
class PlayerPipController(private val activity: ComponentActivity) {
    var showPipUi by mutableStateOf(false)
        private set
    var available by mutableStateOf(false)
        private set
    private var entering by mutableStateOf(false)
    private var token: Any? = null
    private var sessionId = 0
    private var eligible = false
    private var playing = false
    private var aspect = 16f / 9f
    private var bounds: Rect? = null
    private var lastParamsKey: List<Any?>? = null
    private var pausePlayback: (() -> Unit)? = null
    private var playPlayback: (() -> Unit)? = null
    private var closePlayback: (() -> Unit)? = null
    private var receiverRegistered = false
    private var wasInPip = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getIntExtra(EXTRA_SESSION, -1) != sessionId || token == null) return
            when (intent.action) {
                ACTION_PLAY -> playPlayback?.invoke()
                ACTION_PAUSE -> pausePlayback?.invoke()
            }
        }
    }

    val keepPlaybackOnPause: Boolean
        get() = PipPlaybackPolicy.keepOnPause(
            activity.isInPictureInPictureMode,
            entering,
            Build.VERSION.SDK_INT >= 31 && canAutoEnter(),
        )

    fun bind(onPause: () -> Unit, onPlay: () -> Unit, onClose: () -> Unit): Any {
        val binding = Any()
        token = binding
        sessionId++
        eligible = false
        playing = false
        lastParamsKey = null
        pausePlayback = onPause
        playPlayback = onPlay
        closePlayback = onClose
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(
                activity, receiver,
                IntentFilter().apply {
                    addAction(ACTION_PLAY)
                    addAction(ACTION_PAUSE)
                },
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            receiverRegistered = true
        }
        return binding
    }

    fun update(binding: Any, hasVideo: Boolean, wantsPlayback: Boolean, ratio: Float, rect: Rect?) {
        if (binding !== token) return
        eligible = hasVideo
        playing = wantsPlayback
        aspect = PipPlaybackPolicy.aspectRatio(ratio)
        bounds = rect?.takeUnless { it.isEmpty }?.let(::Rect)
        available = eligible && isAllowed()
        updateParams()
    }

    fun unbind(binding: Any) {
        if (binding !== token) return // Old navigation screens must not unbind a newer player.
        clearBinding()
    }

    fun enterManually(): Boolean {
        if (!eligible || !isAllowed()) {
            Toast.makeText(activity, R.string.pip_unavailable, Toast.LENGTH_LONG).show()
            return false
        }
        return enter()
    }

    fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT < 31 && canAutoEnter()) enter()
    }

    fun onPause() {
        if (keepPlaybackOnPause) {
            entering = true
            showPipUi = true
        } else {
            pausePlayback?.invoke()
        }
    }

    fun onResume() {
        if (!activity.isInPictureInPictureMode) {
            entering = false
            showPipUi = false
        }
        available = eligible && isAllowed()
        updateParams()
        // Never auto-play here: a user pause must survive expansion/return.
    }

    fun onModeChanged(inPip: Boolean) {
        entering = false
        showPipUi = inPip
        if (inPip) {
            wasInPip = true
        } else if (!activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            finishPlayback()
        } else {
            wasInPip = false // Visible fullscreen expansion, not dismissal.
        }
    }

    fun onUiTransition(transitioningToPip: Boolean) {
        if (transitioningToPip) {
            entering = true
            showPipUi = true
        }
    }

    fun onStop() {
        // A visible PiP activity is PAUSED, not STOPPED. Once it is no longer
        // visible (close, lock screen, etc.), no hidden background audio remains.
        if (wasInPip || activity.isInPictureInPictureMode) {
            finishPlayback()
        } else {
            entering = false
            showPipUi = false
            pausePlayback?.invoke() // Also covers a denied/failed auto-entry.
        }
    }

    fun destroy() {
        clearBinding()
    }

    private fun finishPlayback() {
        val close = closePlayback
        clearBinding() // Clear first, so mode/stop/disposal cannot close twice.
        close?.invoke()
    }

    private fun clearBinding() {
        token = null
        eligible = false
        playing = false
        entering = false
        wasInPip = false
        showPipUi = false
        available = false
        pausePlayback = null
        playPlayback = null
        closePlayback = null
        updateParams()
        if (receiverRegistered) {
            activity.unregisterReceiver(receiver)
            receiverRegistered = false
        }
    }

    private fun canAutoEnter() = PipPlaybackPolicy.canAutoEnter(eligible, playing, isAllowed())

    private fun isAllowed(): Boolean {
        val pm = activity.packageManager
        if (!pm.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) ||
            pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) return false
        val ops = activity.getSystemService(AppOpsManager::class.java) ?: return false
        return ops.checkOpNoThrow(
            AppOpsManager.OPSTR_PICTURE_IN_PICTURE, activity.applicationInfo.uid, activity.packageName,
        ) == AppOpsManager.MODE_ALLOWED
    }

    private fun enter(): Boolean {
        entering = true
        showPipUi = true
        updateParams()
        val accepted = try {
            activity.enterPictureInPictureMode(buildParams())
        } catch (error: IllegalStateException) {
            Log.w(TAG, "PiP entry unavailable", error)
            false
        } catch (error: IllegalArgumentException) {
            Log.w(TAG, "PiP entry rejected", error)
            false
        }
        if (!accepted) {
            entering = false
            showPipUi = false
            Toast.makeText(activity, R.string.pip_unavailable, Toast.LENGTH_LONG).show()
        }
        return accepted
    }

    private fun updateParams() {
        if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) ||
            activity.isFinishing || activity.isDestroyed) return
        val key = listOf(eligible, playing, available, aspect, bounds, sessionId)
        if (key == lastParamsKey) return
        try {
            activity.setPictureInPictureParams(buildParams())
            lastParamsKey = key
        } catch (error: IllegalStateException) {
            Log.w(TAG, "Cannot update PiP parameters", error)
        } catch (error: IllegalArgumentException) {
            Log.w(TAG, "PiP parameters rejected", error)
        }
    }

    private fun buildParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational((aspect * 10000).toInt(), 10000))
            .setActions(if (eligible) listOf(playPauseAction()) else emptyList())
        bounds?.let(builder::setSourceRectHint)
        if (Build.VERSION.SDK_INT >= 31) {
            builder.setAutoEnterEnabled(eligible && playing && available)
            builder.setSeamlessResizeEnabled(true)
        }
        return builder.build()
    }

    private fun playPauseAction(): RemoteAction {
        val action = if (playing) ACTION_PAUSE else ACTION_PLAY
        val label = activity.getString(if (playing) R.string.pip_pause else R.string.pip_play)
        val broadcast = Intent(action).setPackage(activity.packageName)
            .setData(android.net.Uri.parse("dimodori://pip/$sessionId/$action"))
            .putExtra(EXTRA_SESSION, sessionId)
        val pending = PendingIntent.getBroadcast(
            activity, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return RemoteAction(
            Icon.createWithResource(activity, if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play),
            label, label, pending,
        )
    }

    private companion object {
        const val TAG = "PlayerPip"
        const val ACTION_PLAY = "com.dimodori.app.pip.PLAY"
        const val ACTION_PAUSE = "com.dimodori.app.pip.PAUSE"
        const val EXTRA_SESSION = "pip_session"
    }
}
