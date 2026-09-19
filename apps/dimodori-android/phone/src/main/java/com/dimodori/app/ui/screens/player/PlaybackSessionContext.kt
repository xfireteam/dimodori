package com.dimodori.app.ui.screens.player

internal enum class PlayMethod(
    val displayName: String,
    val reportValue: String
) {
    DIRECT_PLAY(displayName = "Reproducción directa", reportValue = "DirectPlay"),
    DIRECT_STREAM(displayName = "Transmisión directa", reportValue = "DirectStream"),
    TRANSCODE(displayName = "Transcodificación", reportValue = "Transcode"),
    OFFLINE(displayName = "Sin conexión", reportValue = "DirectPlay")
}

internal data class PlaybackSessionContext(
    val mediaId: String? = null,
    val playSessionId: String? = null,
    val mediaSourceId: String? = null,
    val mediaSourceContainer: String? = null,
    val mediaSourceBitrateKbps: Int? = null,
    val playMethod: PlayMethod = PlayMethod.DIRECT_PLAY,
    val isOfflinePlayback: Boolean = false
)
