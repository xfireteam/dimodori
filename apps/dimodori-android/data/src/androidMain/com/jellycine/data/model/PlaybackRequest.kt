package com.jellycine.data.model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jellycine.data.R
import com.jellycine.data.network.ServerType
import com.jellycine.data.util.buildServerUrl
import com.jellycine.data.util.getServerUrl
import com.jellycine.data.util.removeQueryParameter
import java.net.URI

private const val API_KEY_QUERY_PARAM = "api_key"

data class PlaybackRequest(
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap()
) {
    fun authorizeRelatedUrl(relatedUrl: String): String {
        val apiKey = url.apiKey() ?: return relatedUrl
        if (relatedUrl.apiKey() != null) return relatedUrl
        return Uri.parse(relatedUrl).buildUpon()
            .appendQueryParameter(API_KEY_QUERY_PARAM, apiKey)
            .build()
            .toString()
    }
}

private fun String.apiKey(): String? {
    return Uri.parse(this).getQueryParameter(API_KEY_QUERY_PARAM)?.takeIf { it.isNotBlank() }
}

internal data class PlaybackAuthContext(
    val serverUrl: String,
    val serverType: ServerType?,
    val accessToken: String?,
    val deviceId: String,
    val clientVersion: String
)

internal data class PlaybackStreamOptions(
    val maxStreamingBitrate: Int? = null,
    val maxStreamingHeight: Int? = null,
    val audioStreamIndex: Int? = null,
    val subtitleStreamIndex: Int? = null,
    val audioTranscodeMode: AudioTranscodeMode = AudioTranscodeMode.AUTO,
    val includeAccessToken: Boolean = false
)

internal object PlaybackUrlBuilder {
    fun playbackInfoUrls(
        serverUrl: String,
        playbackInfo: PlaybackInfoResponse
    ): PlaybackInfoResponse {
        return playbackInfo.copy(
            mediaSources = playbackInfo.mediaSources?.map { mediaSource ->
                mediaSource.copy(
                    transcodingUrl = getServerUrl(
                        baseUrl = serverUrl,
                        url = mediaSource.transcodingUrl
                    ) ?: mediaSource.transcodingUrl,
                    mediaStreams = mediaSource.mediaStreams?.map { mediaStream ->
                        mediaStream.copy(
                            deliveryUrl = resolvePlaybackUrl(
                                serverUrl = serverUrl,
                                url = mediaStream.deliveryUrl
                            ),
                            path = finalUrl(
                                baseUrl = serverUrl,
                                url = mediaStream.path
                            )
                        )
                    },
                    mediaAttachments = mediaSource.mediaAttachments?.map { mediaAttachment ->
                        mediaAttachment.copy(
                            deliveryUrl = resolvePlaybackUrl(
                                serverUrl = serverUrl,
                                url = mediaAttachment.deliveryUrl
                            )
                        )
                    }
                )
            }
        )
    }

    fun createLocalPlaybackRequest(
        context: Context,
        authContext: PlaybackAuthContext,
        itemId: String,
        playbackInfo: PlaybackInfoResponse,
        options: PlaybackStreamOptions
    ): Result<PlaybackRequest> {
        return buildStreamingUrl(
            context = context,
            authContext = authContext,
            itemId = itemId,
            playbackInfo = playbackInfo,
            options = options
        ).mapCatching { streamingUrl ->
            PlaybackRequest(
                url = streamingUrl,
                requestHeaders = if (
                    options.includeAccessToken ||
                    isExternalPlaybackUrl(streamingUrl, authContext.serverUrl)
                ) {
                    emptyMap()
                } else {
                    buildPlaybackRequestHeaders(authContext)
                }
            )
        }
    }

    fun createCastStreamingUrl(
        context: Context,
        authContext: PlaybackAuthContext,
        itemId: String,
        playbackInfo: PlaybackInfoResponse,
        options: PlaybackStreamOptions
    ): Result<String> {
        return buildStreamingUrl(
            context = context,
            authContext = authContext,
            itemId = itemId,
            playbackInfo = playbackInfo,
            options = options.copy(includeAccessToken = true)
        )
    }

    private fun buildPlaybackRequestHeaders(authContext: PlaybackAuthContext): Map<String, String> {
        if (authContext.accessToken.isNullOrBlank()) {
            return emptyMap()
        }

        val authHeader = AuthHeaderDto.fromServerType(
            serverType = authContext.serverType,
            deviceId = authContext.deviceId,
            version = authContext.clientVersion,
            accessToken = authContext.accessToken
        ).asHeaderValue()

        return mapOf(
            "Authorization" to authHeader,
            "X-Emby-Authorization" to authHeader
        )
    }

    private fun isExternalPlaybackUrl(url: String, serverUrl: String): Boolean {
        val source = runCatching { URI.create(url) }.getOrNull() ?: return false
        val server = runCatching { URI.create(serverUrl) }.getOrNull() ?: return false
        if (source.host.isNullOrBlank()) return false
        if (server.host.isNullOrBlank()) return true
        return !source.host.equals(server.host, ignoreCase = true) ||
            !source.scheme.equals(server.scheme, ignoreCase = true) ||
            effectivePort(source) != effectivePort(server)
    }

    private fun effectivePort(uri: URI): Int = when {
        uri.port >= 0 -> uri.port
        uri.scheme.equals("https", ignoreCase = true) -> 443
        uri.scheme.equals("http", ignoreCase = true) -> 80
        else -> -1
    }

    private fun buildStreamingUrl(
        context: Context,
        authContext: PlaybackAuthContext,
        itemId: String,
        playbackInfo: PlaybackInfoResponse,
        options: PlaybackStreamOptions
    ): Result<String> {
        return try {
            val mediaSource = playbackInfo.mediaSources?.firstOrNull()
                ?: return Result.failure(Exception(context.getString(R.string.data_error_no_media_source_available)))
            val normalizedSubtitleStreamIndex = normalizeSubtitleStreamIndex(options.subtitleStreamIndex)
            val selectedAudioStream = getSelectedAudioStream(
                mediaSource = mediaSource,
                requestedAudioStreamIndex = options.audioStreamIndex
            )
            val hasQualityCap = (options.maxStreamingBitrate ?: 0) > 0 || (options.maxStreamingHeight ?: 0) > 0
            val needsAudioTranscoding = needsAudioTranscode(
                audioTranscodeMode = options.audioTranscodeMode,
                selectedAudioStream = selectedAudioStream
            )
            if (authContext.serverType == ServerType.EMBY && mediaSource.isRemote == true) {
                Log.i(
                    "PlaybackUrlBuilder",
                    "Remote source: direct=${mediaSource.supportsDirectPlay}, " +
                        "stream=${mediaSource.supportsDirectStream}, " +
                        "transcode=${!mediaSource.transcodingUrl.isNullOrBlank()}, " +
                        "headers=${!mediaSource.requiredHttpHeaders.isNullOrEmpty()}"
                )
            }

            val serverTranscodingUrl = !mediaSource.transcodingUrl.isNullOrBlank() &&
                (
                    hasQualityCap ||
                        needsAudioTranscoding ||
                        (mediaSource.supportsDirectPlay != true &&
                            mediaSource.supportsDirectStream != true)
                )
            if (serverTranscodingUrl) {
                val resolvedTranscodingUrl = getServerUrl(
                    baseUrl = authContext.serverUrl,
                    url = mediaSource.transcodingUrl
                )
                if (!resolvedTranscodingUrl.isNullOrBlank()) {
                    if (mediaSource.isRemote == true) {
                        Log.i("PlaybackUrlBuilder", "Remote source: server transcode selected")
                    }
                    val selectedTranscodingUrl = if (authContext.serverType == ServerType.JELLYFIN) {
                        resolvedTranscodingUrl
                    } else {
                        applyTranscodingSelectionOverrides(
                            streamingUrl = resolvedTranscodingUrl,
                            audioStreamIndex = options.audioStreamIndex,
                            audioTranscodeMode = options.audioTranscodeMode,
                            sourceVideoBitrate = mediaSource.bitrate,
                            preserveOriginalVideo = !hasQualityCap && needsAudioTranscoding
                        )
                    }
                    return Result.success(
                        appendToken(
                            url = selectedTranscodingUrl,
                            authContext = authContext,
                            options = options
                        )
                    )
                }
            }

            // Emby STRM items may expose the actual HTTP stream as a remote media source.
            // Keep server streaming for local files, transcoding, or sources needing special headers.
            if (
                authContext.serverType == ServerType.EMBY &&
                mediaSource.isRemote == true &&
                mediaSource.supportsDirectPlay == true &&
                !hasQualityCap &&
                !needsAudioTranscoding &&
                mediaSource.requiredHttpHeaders.isNullOrEmpty()
            ) {
                mediaSource.path?.trim()?.let { remoteUrl ->
                    val remoteUri = runCatching { URI.create(remoteUrl) }.getOrNull()
                    if (
                        remoteUri?.scheme?.lowercase() in setOf("http", "https") &&
                        !remoteUri?.host.isNullOrBlank() &&
                        isExternalPlaybackUrl(remoteUrl, authContext.serverUrl)
                    ) {
                        Log.i("PlaybackUrlBuilder", "Remote source: external HTTP direct play selected")
                        return Result.success(remoteUrl)
                    }
                }
            }

            val streamQueryParams = mutableListOf<Pair<String, String?>>()
            streamQueryParams.add("mediaSourceId" to mediaSource.id)
            options.audioStreamIndex?.let { streamQueryParams.add("audioStreamIndex" to it.toString()) }
            normalizeSubtitleStreamIndex(options.subtitleStreamIndex)?.let {
                streamQueryParams.add("subtitleStreamIndex" to it.toString())
            }
            streamQueryParams.add("PlaySessionId" to playbackInfo.playSessionId)
            streamQueryParams.add("DeviceId" to authContext.deviceId)
            if (options.includeAccessToken) {
                authContext.accessToken?.takeIf { it.isNotBlank() }?.let { accessToken ->
                    streamQueryParams.add(API_KEY_QUERY_PARAM to accessToken)
                }
            }

            if (hasQualityCap) {
                return Result.failure(
                    Exception(context.getString(R.string.data_error_transcoding_url_unavailable, itemId))
                )
            }

            val streamingUrl = buildStreamUrl(
                serverUrl = authContext.serverUrl,
                itemId = itemId,
                queryParams = streamQueryParams,
                useStaticStream = mediaSource.supportsDirectPlay == true
            )
            if (mediaSource.isRemote == true) {
                Log.i("PlaybackUrlBuilder", "Remote source: server stream selected")
            }

            Result.success(streamingUrl)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun normalizeSubtitleStreamIndex(subtitleStreamIndex: Int?): Int? {
        return subtitleStreamIndex?.takeIf { it >= 0 }
    }

    private fun resolvePlaybackUrl(
        serverUrl: String,
        url: String?
    ): String? {
        return finalUrl(serverUrl, url)?.let { parsedUrl ->
            removeQueryParameter(parsedUrl, API_KEY_QUERY_PARAM)
        }
    }

    private fun finalUrl(baseUrl: String, url: String?): String? {
        return getServerUrl(baseUrl = baseUrl, url = url) ?: url
    }

    private fun appendToken(
        url: String,
        authContext: PlaybackAuthContext,
        options: PlaybackStreamOptions
    ): String {
        if (!options.includeAccessToken) return url
        if (isExternalPlaybackUrl(url, authContext.serverUrl)) return url
        val apiKey = authContext.accessToken?.takeIf { it.isNotBlank() } ?: return url
        if (url.apiKey() != null) return url
        return Uri.parse(url).buildUpon()
            .appendQueryParameter(API_KEY_QUERY_PARAM, apiKey)
            .build()
            .toString()
    }

    private fun buildStreamUrl(
        serverUrl: String,
        itemId: String,
        queryParams: List<Pair<String, String?>>,
        useStaticStream: Boolean
    ): String {
        val finalQueryParams = if (useStaticStream) {
            buildList {
                add("static" to "true")
                addAll(queryParams)
            }
        } else {
            queryParams
        }
        return buildServerUrl(
            baseUrl = serverUrl,
            encodedPath = "Videos/$itemId/stream",
            queryParams = finalQueryParams
        )
    }

    private fun getSelectedAudioStream(
        mediaSource: MediaSource,
        requestedAudioStreamIndex: Int?
    ): MediaStream? {
        val audioStreams = mediaSource.mediaStreams
            ?.filter { stream -> stream.type.equals("Audio", ignoreCase = true) }
            .orEmpty()
        if (audioStreams.isEmpty()) {
            return null
        }

        val targetIndex = requestedAudioStreamIndex
            ?: mediaSource.defaultAudioStreamIndex
            ?: audioStreams.firstOrNull { it.isDefault == true }?.index

        return audioStreams.firstOrNull { stream -> stream.index == targetIndex }
            ?: audioStreams.first()
    }

    private fun needsAudioTranscode(
        audioTranscodeMode: AudioTranscodeMode,
        selectedAudioStream: MediaStream?
    ): Boolean {
        val codec = selectedAudioStream?.codec?.lowercase()
        return when (audioTranscodeMode) {
            AudioTranscodeMode.STEREO -> {
                val channels = selectedAudioStream?.channels ?: 2
                channels > 2 || codec !in setOf("aac", "mp3", "opus", "vorbis", "pcm")
            }
            AudioTranscodeMode.SURROUND_5_1 -> codec != "eac3"
            else -> false
        }
    }

    private fun applyTranscodingSelectionOverrides(
        streamingUrl: String,
        audioStreamIndex: Int?,
        audioTranscodeMode: AudioTranscodeMode = AudioTranscodeMode.AUTO,
        sourceVideoBitrate: Int? = null,
        preserveOriginalVideo: Boolean = false
    ): String {
        val sourceUri = Uri.parse(streamingUrl)
        val builder = sourceUri.buildUpon().clearQuery()
        val overrideParams = linkedMapOf<String, String>()

        audioStreamIndex?.let {
            overrideParams["AudioStreamIndex"] = it.toString()
        }

        if (preserveOriginalVideo) {
            overrideParams["allowVideoStreamCopy"] = "true"
            overrideParams["allowAudioStreamCopy"] = "false"
            sourceVideoBitrate?.takeIf { it > 0 }?.let { bitrate ->
                overrideParams["VideoBitrate"] = bitrate.toString()
            }
            when (audioTranscodeMode) {
                AudioTranscodeMode.STEREO -> {
                    overrideParams["AudioCodec"] = "aac"
                    overrideParams["TranscodingMaxAudioChannels"] = "2"
                }
                AudioTranscodeMode.SURROUND_5_1 -> {
                    overrideParams["AudioCodec"] = "eac3"
                    overrideParams["TranscodingMaxAudioChannels"] = "6"
                }
                else -> Unit
            }
        } else if (audioTranscodeMode == AudioTranscodeMode.PASSTHROUGH) {
            overrideParams["allowAudioStreamCopy"] = "true"
        }

        sourceUri.queryParameterNames
            .filterNot { queryName ->
                overrideParams.keys.any { key ->
                    queryName.equals(key, ignoreCase = true)
                }
            }
            .forEach { queryName ->
                sourceUri.getQueryParameters(queryName).forEach { queryValue ->
                    builder.appendQueryParameter(queryName, queryValue)
                }
            }

        overrideParams.forEach { (key, value) ->
            builder.appendQueryParameter(key, value)
        }

        return builder.build().toString()
    }
}