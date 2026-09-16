package com.dimodori.app.ui.screens.auth

import com.dimodori.app.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

internal object ServerSelectorRemoteConfig {
    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .callTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun shouldHideServerSelector(): Boolean = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.SERVER_SELECTOR_CONFIG_URL.trim()
        if (endpoint.isBlank()) return@withContext false

        runCatching {
            val request = Request.Builder()
                .url(endpoint)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use false

                val responseBody = response.body.string()
                JSONObject(responseBody).opt("hideServerSelector") as? Boolean ?: false
            }
        }.getOrDefault(false)
    }
}