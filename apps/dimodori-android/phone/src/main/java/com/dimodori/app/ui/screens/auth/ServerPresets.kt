package com.dimodori.app.ui.screens.auth

internal data class PresetServer(
    val section: String,
    val label: String,
    val url: String,
    val localUrl: String
)

internal val PresetServer.key: String
    get() = "$section|$label"

internal val presetServers = listOf(
    PresetServer(
        "DIMODORI PREMIUM",
        "S1",
        "http://em.dimodori.com:8100",
        "http://192.168.0.246:8100"
    ),
    PresetServer(
        "DIMODORI PREMIUM",
        "S2",
        "http://em.dimodori.com:8098",
        "http://192.168.0.248:8098"
    ),
    PresetServer(
        "DIMODORI PREMIUM",
        "S3",
        "http://em.dimodori.com:8106",
        "http://192.168.0.240:8106"
    ),
    PresetServer(
        "DIMODORI BASIC",
        "S1",
        "http://em.dimodori.com:8920",
        "http://192.168.0.236:8920"
    ),
    PresetServer(
        "DIMODORI BASIC",
        "S2",
        "http://em.dimodori.com:8102",
        "http://192.168.0.234:8102"
    ),
    PresetServer(
        "DIMODORI BASIC",
        "S3",
        "http://em.dimodori.com:8104",
        "http://192.168.0.236:8104"
    ),
    PresetServer(
        "DIMODORI",
        "JELLYFIN",
        "http://em.dimodori.com:32404",
        "http://192.168.0.246:32400"
    )
)