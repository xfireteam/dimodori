package com.jellycine.data.network

import kotlinx.serialization.json.Json

val DimodoriJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    isLenient = true
}
