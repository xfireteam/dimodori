package com.dimodori.app.ui.screens.dashboard.settings

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.HdrOn
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoSettings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dimodori.app.ui.components.common.AmoledSelectionDialog
import com.dimodori.app.ui.components.common.SelectionOption
import com.jellycine.player.preferences.PlayerPreferences
import com.jellycine.shared.R
import com.jellycine.shared.ui.components.common.Slider
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsScreen(
    onBackPressed: () -> Unit = {},
    onNavigateToSubtitleSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PlayerSettingsViewModel = viewModel { PlayerSettingsViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    val decodingColor = Color(0xFF3B82F6)
    val transcodingColor = Color(0xFF8B5CF6)
    val videoColor = Color(0xFFF97316)
    val gesturesColor = Color(0xFF14B8A6)
    val seekingColor = Color(0xFFEF4444)
    val performanceColor = Color(0xFF22C55E)
    val cacheColor = Color(0xFF06B6D4)

    Scaffold(
        topBar = {
            topbar(
                title = stringResource(R.string.player_settings_title),
                onBackPressed = onBackPressed
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item { SectionLabel(stringResource(R.string.player_settings_section_player)) }
            item {
                SettingsSection {
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.VideoSettings,
                        title = stringResource(R.string.player_settings_player_engine),
                        subtitle = playerEngineLabel(uiState.playerEngine),
                        selectedValue = uiState.playerEngine,
                        options = listOf(
                            SelectionOption("ExoPlayer", "ExoPlayer", "Nativo de Android, eficiente con la batería", isDefault = true),
                            SelectionOption("MPV", "MPV", "Renderizado avanzado, sombreadores personalizados, mejor HDR")
                        ),
                        onOptionSelected = viewModel::setPlayerEngine,
                        accentColor = videoColor
                    )

                    if (uiState.playerEngine == PlayerPreferences.PLAYER_ENGINE_MPV) {
                        SettingsDivider()
                        SelectionDialogSettingsItem(
                            icon = Icons.Rounded.Speed,
                            title = stringResource(R.string.player_settings_mpv_hardware_decoding),
                            subtitle = hardwareDecodingLabel(uiState.mpvHardwareDecoding),
                            selectedValue = uiState.mpvHardwareDecoding,
                            options = listOf(
                                SelectionOption("mediacodec", "MediaCodec", "Decodificación directa por hardware, máximo rendimiento", isDefault = true),
                                SelectionOption("mediacodec-copy", "MediaCodec (copia)", "Decodificación por hardware con copia a la CPU, mayor compatibilidad de formatos"),
                                SelectionOption("no", "Software", "Decodificación por CPU, máxima compatibilidad")
                            ),
                            onOptionSelected = viewModel::setMpvHardwareDecoding,
                            accentColor = videoColor
                        )

                        SettingsDivider()
                        SelectionDialogSettingsItem(
                            icon = Icons.Rounded.VideoSettings,
                            title = stringResource(R.string.player_settings_mpv_video_output),
                            subtitle = videoOutputLabel(uiState.mpvVideoOutput),
                            selectedValue = uiState.mpvVideoOutput,
                            options = listOf(
                                SelectionOption("gpu-next", "GPU Next", "Renderizador libplacebo moderno, máxima calidad", isDefault = true),
                                SelectionOption("gpu", "GPU", "Renderizador GPU heredado, compatible con más dispositivos")
                            ),
                            onOptionSelected = viewModel::setMpvVideoOutput,
                            accentColor = videoColor
                        )

                        SettingsDivider()
                        SelectionDialogSettingsItem(
                            icon = Icons.Rounded.AudioFile,
                            title = stringResource(R.string.player_settings_mpv_audio_output),
                            subtitle = audioOutputLabel(uiState.mpvAudioOutput),
                            selectedValue = uiState.mpvAudioOutput,
                            options = listOf(
                                SelectionOption("audiotrack", "AudioTrack", "Audio estándar de Android, máxima compatibilidad", isDefault = true),
                                SelectionOption("aaudio", "AAudio", "Audio de baja latencia, Android 8.1+"),
                                SelectionOption("opensles", "OpenSL ES", "Audio heredado de baja latencia")
                            ),
                            onOptionSelected = viewModel::setMpvAudioOutput,
                            accentColor = videoColor
                        )
                    }
                }
            }

            if (uiState.playerEngine == PlayerPreferences.PLAYER_ENGINE_MPV) {
                val renderingColor = Color(0xFFF59E0B)
                item { SectionLabel("RENDERIZADO") }
                item {
                    SettingsSection {
                        SelectionDialogSettingsItem(
                            icon = Icons.Rounded.Tune,
                            title = "Filtro de ampliación",
                            subtitle = upscaleFilterLabel(uiState.mpvUpscaleFilter),
                            selectedValue = uiState.mpvUpscaleFilter,
                            options = listOf(
                                SelectionOption("bilinear", "Bilinear", "Rápido, suave y ligeramente difuso"),
                                SelectionOption("spline36", "Spline36", "Equilibrio entre nitidez y suavidad"),
                                SelectionOption("lanczos", "Lanczos", "Nítido y detallado", isDefault = true),
                                SelectionOption("ewa_lanczos", "EWA Lanczos", "Máxima nitidez, uso intensivo de GPU")
                            ),
                            onOptionSelected = viewModel::setMpvUpscaleFilter,
                            accentColor = renderingColor
                        )

                        SettingsDivider()
                        SelectionDialogSettingsItem(
                            icon = Icons.Rounded.Tune,
                            title = "Filtro de reducción",
                            subtitle = downscaleFilterLabel(uiState.mpvDownscaleFilter),
                            selectedValue = uiState.mpvDownscaleFilter,
                            options = listOf(
                                SelectionOption("hermite", "Hermite", "Suave y blando, reduce el aliasing", isDefault = true),
                                SelectionOption("mitchell", "Mitchell", "Equilibrado, con algo de nitidez"),
                                SelectionOption("catmull_rom", "Catmull-Rom", "Más nítido, conserva los bordes"),
                                SelectionOption("lanczos", "Lanczos", "Reducción más nítida, puede producir ligeros halos")
                            ),
                            onOptionSelected = viewModel::setMpvDownscaleFilter,
                            accentColor = renderingColor
                        )

                        SettingsDivider()
                        SelectionDialogSettingsItem(
                            icon = Icons.Rounded.Contrast,
                            title = "Mapeo de tonos",
                            subtitle = toneMappingLabel(uiState.mpvToneMapping),
                            selectedValue = uiState.mpvToneMapping,
                            options = listOf(
                                SelectionOption("auto", "Auto", "Elige dinámicamente el mejor algoritmo para cada escena", isDefault = true),
                                SelectionOption("bt.2390", "BT.2390", "Estándar de emisión, luces naturales"),
                                SelectionOption("spline", "Spline", "Curva suave, conserva los tonos medios"),
                                SelectionOption("hable", "Filmic", "Transición cinematográfica, luces más suaves"),
                                SelectionOption("mobius", "Mobius", "Transición suave, conserva el detalle en sombras"),
                                SelectionOption("reinhard", "Reinhard", "Sencillo, luces ligeramente desaturadas")
                            ),
                            onOptionSelected = viewModel::setMpvToneMapping,
                            enabled = uiState.mpvHdrToSdrTonemapping,
                            accentColor = renderingColor
                        )

                        SettingsDivider()
                        SwitchSettingsItem(
                            icon = Icons.Rounded.SlowMotionVideo,
                            title = "Movimiento fluido",
                            subtitle = "Interpola fotogramas en pantallas de alta frecuencia de actualización",
                            checked = uiState.mpvSmoothMotion,
                            onCheckedChange = viewModel::setMpvSmoothMotion,
                            accentColor = renderingColor
                        )

                        SettingsDivider()
                        SwitchSettingsItem(
                            icon = Icons.Rounded.Gradient,
                            title = "Deband",
                            subtitle = "Reduce las bandas de color en degradados oscuros",
                            checked = uiState.mpvDeband,
                            onCheckedChange = viewModel::setMpvDeband,
                            accentColor = renderingColor
                        )

                        SettingsDivider()
                        SwitchSettingsItem(
                            icon = Icons.Rounded.WbSunny,
                            title = "Brillo dinámico",
                            subtitle = "Adapta el mapeo de tonos a cada escena",
                            checked = uiState.mpvDynamicPeak,
                            onCheckedChange = viewModel::setMpvDynamicPeak,
                            enabled = uiState.mpvHdrToSdrTonemapping,
                            accentColor = renderingColor
                        )
                        SettingsDivider()
                        SwitchSettingsItem(
                            icon = Icons.Rounded.HdrOn,
                            title = "Mapeo de tonos de HDR a SDR",
                            subtitle = "Mapea el contenido HDR a SDR para obtener colores precisos",
                            checked = uiState.mpvHdrToSdrTonemapping,
                            onCheckedChange = viewModel::setMpvHdrToSdrTonemapping,
                            accentColor = renderingColor
                        )
                    }
                }
            }

            item { SectionLabel(stringResource(R.string.player_settings_section_decoding)) }
            item {
                SettingsSection {
                    SwitchSettingsItem(
                        icon = Icons.Rounded.Speed,
                        title = stringResource(R.string.player_settings_hardware_acceleration),
                        subtitle = stringResource(R.string.player_settings_hardware_acceleration_summary),
                        checked = uiState.hardwareDecodingEnabled,
                        onCheckedChange = viewModel::setHardwareDecodingEnabled,
                        accentColor = decodingColor
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SettingsDivider()
                        SwitchSettingsItem(
                            icon = Icons.Rounded.Tune,
                            title = stringResource(R.string.player_settings_async_mediacodec),
                            subtitle = stringResource(R.string.player_settings_async_mediacodec_summary),
                            checked = uiState.asyncMediaCodecEnabled,
                            onCheckedChange = viewModel::setAsyncMediaCodecEnabled,
                            enabled = uiState.hardwareDecodingEnabled,
                            accentColor = decodingColor
                        )
                    }
                }
            }

            item { SectionLabel(stringResource(R.string.player_settings_section_subtitles)) }
            item {
                SettingsSection {
                    ClickableSettingsItem(
                        icon = Icons.Rounded.VideoSettings,
                        title = stringResource(R.string.player_settings_subtitles),
                        subtitle = stringResource(R.string.player_settings_subtitles_summary),
                        onClick = onNavigateToSubtitleSettings,
                        accentColor = Color(0xFF6366F1)
                    )
                }
            }

            if (uiState.isVideoTranscodingAllowed || uiState.isAudioTranscodingAllowed) {
                item { SectionLabel(stringResource(R.string.player_settings_section_transcoding)) }
                item {
                    SettingsSection {
                        if (uiState.isVideoTranscodingAllowed) {
                            SelectionDialogSettingsItem(
                                icon = Icons.Rounded.HighQuality,
                                title = stringResource(R.string.player_settings_streaming_quality),
                                subtitle = uiState.streamingQuality,
                                selectedValue = uiState.streamingQuality,
                                options = PlayerPreferences.STREAMING_QUALITY_OPTIONS.map { quality ->
                                    SelectionOption(
                                        value = quality,
                                        label = quality,
                                        description = "",
                                        isDefault = quality == PlayerPreferences.DEFAULT_STREAMING_QUALITY
                                    )
                                },
                                onOptionSelected = viewModel::setStreamingQuality,
                                accentColor = transcodingColor
                            )
                        }

                        if (uiState.isVideoTranscodingAllowed && uiState.isAudioTranscodingAllowed) {
                            SettingsDivider()
                        }

                        if (uiState.isAudioTranscodingAllowed) {
                            SelectionDialogSettingsItem(
                                icon = Icons.Rounded.AudioFile,
                                title = stringResource(R.string.player_settings_audio_quality),
                                subtitle = audioTranscodeModeLabel(uiState.audioTranscodeMode),
                                selectedValue = uiState.audioTranscodeMode,
                                options = listOf(
                                    SelectionOption("Auto", "Auto", "El servidor decide según las capacidades del cliente", isDefault = true),
                                    SelectionOption("Stereo", "Estéreo", "Audio de 2 canales, máxima compatibilidad"),
                                    SelectionOption("5.1 Surround", "Sonido envolvente 5.1", "Sonido envolvente de 6 canales"),
                                    SelectionOption("Passthrough", "Transferencia directa", "Audio original, sin transcodificación")
                                ),
                                onOptionSelected = viewModel::setAudioTranscodeMode,
                                accentColor = transcodingColor
                            )
                        }
                    }
                }
            }

            item { SectionLabel(stringResource(R.string.player_settings_section_video)) }
            item {
                SettingsSection {
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.VideoSettings,
                        title = stringResource(R.string.player_settings_decoder_priority),
                        subtitle = decoderPriorityLabel(uiState.decoderPriority),
                        selectedValue = uiState.decoderPriority,
                        options = listOf(
                            SelectionOption(PlayerPreferences.DECODER_PRIORITY_AUTO, "Auto", "Deja que el reproductor decida según el contenido", isDefault = true),
                            SelectionOption(PlayerPreferences.DECODER_PRIORITY_HARDWARE, "Hardware primero", "Prefiere la decodificación por GPU para consumir menos batería"),
                            SelectionOption(PlayerPreferences.DECODER_PRIORITY_SOFTWARE, "Software primero", "Prefiere la decodificación por CPU para admitir más códecs")
                        ),
                        onOptionSelected = viewModel::setDecoderPriority,
                        accentColor = videoColor
                    )

                    SettingsDivider()
                    SwitchSettingsItem(
                        icon = Icons.Rounded.Fullscreen,
                        title = stringResource(R.string.player_settings_start_maximized),
                        subtitle = stringResource(R.string.player_settings_start_maximized_summary),
                        checked = uiState.startMaximized,
                        onCheckedChange = viewModel::setStartMaximized,
                        accentColor = videoColor
                    )

                    SettingsDivider()
                    SwitchSettingsItem(
                        icon = Icons.Rounded.Devices,
                        title = stringResource(R.string.player_settings_use_device_volume_in_player),
                        subtitle = stringResource(R.string.player_settings_use_device_volume_in_player_summary),
                        checked = uiState.useDeviceVolumeInPlayer,
                        onCheckedChange = viewModel::setUseDeviceVolumeInPlayer,
                        accentColor = videoColor
                    )

                    SettingsDivider()
                    SwitchSettingsItem(
                        icon = Icons.Rounded.DisplaySettings,
                        title = stringResource(R.string.player_settings_use_device_brightness_in_player),
                        subtitle = stringResource(R.string.player_settings_use_device_brightness_in_player_summary),
                        checked = uiState.useDeviceBrightnessInPlayer,
                        onCheckedChange = viewModel::setUseDeviceBrightnessInPlayer,
                        accentColor = videoColor
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.player_settings_section_gestures)) }
            item {
                SettingsSection {
                    SwitchSettingsItem(
                        icon = Icons.Rounded.Tune,
                        title = stringResource(R.string.player_settings_gestures),
                        subtitle = stringResource(R.string.player_settings_gestures_summary),
                        checked = uiState.playerGesturesEnabled,
                        onCheckedChange = viewModel::setPlayerGesturesEnabled,
                        accentColor = gesturesColor
                    )

                    SettingsDivider()
                    SwitchSettingsItem(
                        icon = Icons.Rounded.Brush,
                        title = stringResource(R.string.player_settings_volume_brightness_gestures),
                        subtitle = stringResource(R.string.player_settings_volume_brightness_gestures_summary),
                        checked = uiState.volumeBrightnessGesturesEnabled,
                        onCheckedChange = viewModel::setVolumeBrightnessGesturesEnabled,
                        enabled = uiState.playerGesturesEnabled,
                        accentColor = gesturesColor
                    )

                    SettingsDivider()
                    SwitchSettingsItem(
                        icon = Icons.Rounded.FastForward,
                        title = stringResource(R.string.player_settings_progress_seek_gesture),
                        subtitle = stringResource(R.string.player_settings_progress_seek_gesture_summary),
                        checked = uiState.progressSeekGestureEnabled,
                        onCheckedChange = viewModel::setProgressSeekGestureEnabled,
                        enabled = uiState.playerGesturesEnabled,
                        accentColor = gesturesColor
                    )

                    SettingsDivider()
                    SwitchSettingsItem(
                        icon = Icons.Rounded.Fullscreen,
                        title = stringResource(R.string.player_settings_zoom_gesture),
                        subtitle = stringResource(R.string.player_settings_zoom_gesture_summary),
                        checked = uiState.zoomGestureEnabled,
                        onCheckedChange = viewModel::setZoomGestureEnabled,
                        enabled = uiState.playerGesturesEnabled,
                        accentColor = gesturesColor
                    )

                }
            }

            item { SectionLabel(stringResource(R.string.player_settings_section_seeking)) }
            item {
                SettingsSection {
                    SwitchSettingsItem(
                        icon = Icons.Rounded.SkipNext,
                        title = stringResource(R.string.player_settings_skip_intro),
                        subtitle = stringResource(R.string.player_settings_skip_intro_summary),
                        checked = uiState.skipIntroEnabled,
                        onCheckedChange = viewModel::setSkipIntroEnabled,
                        accentColor = seekingColor
                    )

                    SettingsDivider()
                    SwitchSettingsItem(
                        icon = Icons.Rounded.Schedule,
                        title = stringResource(R.string.player_settings_chapter_markers),
                        subtitle = stringResource(R.string.player_settings_chapter_markers_summary),
                        checked = uiState.chapterMarkersEnabled,
                        onCheckedChange = viewModel::setChapterMarkersEnabled,
                        accentColor = seekingColor
                    )

                    SettingsDivider()
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.FastRewind,
                        title = stringResource(R.string.player_settings_seek_backward),
                        subtitle = stringResource(
                            R.string.player_settings_seek_interval_value,
                            uiState.seekBackwardIntervalSeconds
                        ),
                        selectedValue = uiState.seekBackwardIntervalSeconds.toString(),
                        options = (PlayerPreferences.MIN_SEEK_INTERVAL_SECONDS..PlayerPreferences.MAX_SEEK_INTERVAL_SECONDS step PlayerPreferences.SEEK_INTERVAL_STEP_SECONDS)
                            .map { seconds ->
                                SelectionOption(
                                    value = seconds.toString(),
                                    label = "${seconds}s",
                                    description = "",
                                    isDefault = seconds == PlayerPreferences.DEFAULT_SEEK_INTERVAL_SECONDS
                                )
                            },
                        onOptionSelected = { seconds ->
                            viewModel.setSeekBackwardIntervalSeconds(seconds.toInt())
                        },
                        accentColor = seekingColor
                    )

                    SettingsDivider()
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.FastForward,
                        title = stringResource(R.string.player_settings_seek_forward),
                        subtitle = stringResource(
                            R.string.player_settings_seek_interval_value,
                            uiState.seekForwardIntervalSeconds
                        ),
                        selectedValue = uiState.seekForwardIntervalSeconds.toString(),
                        options = (PlayerPreferences.MIN_SEEK_INTERVAL_SECONDS..PlayerPreferences.MAX_SEEK_INTERVAL_SECONDS step PlayerPreferences.SEEK_INTERVAL_STEP_SECONDS)
                            .map { seconds ->
                                SelectionOption(
                                    value = seconds.toString(),
                                    label = "${seconds}s",
                                    description = "",
                                    isDefault = seconds == PlayerPreferences.DEFAULT_SEEK_INTERVAL_SECONDS
                                )
                            },
                        onOptionSelected = { seconds ->
                            viewModel.setSeekForwardIntervalSeconds(seconds.toInt())
                        },
                        accentColor = seekingColor
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.player_settings_section_player_cache)) }
            item {
                SettingsSection {
                    SwitchSettingsItem(
                        icon = Icons.Rounded.SkipNext,
                        title = stringResource(R.string.cache_settings_cache_next_episode),
                        subtitle = stringResource(R.string.cache_settings_cache_next_episode_summary),
                        checked = uiState.cacheNextEpisodeEnabled,
                        onCheckedChange = viewModel::setCacheNextEpisodeEnabled,
                        accentColor = cacheColor
                    )

                    SettingsDivider()
                    ValueSliderSettingsItem(
                        icon = Icons.Rounded.Storage,
                        title = stringResource(R.string.player_settings_player_cache_size),
                        subtitle = stringResource(R.string.player_settings_player_cache_size_summary),
                        value = uiState.playerCacheSizeMb,
                        defaultValue = PlayerPreferences.DEFAULT_PLAYER_CACHE_SIZE_MB,
                        minValue = PlayerPreferences.MIN_PLAYER_CACHE_SIZE_MB,
                        maxValue = PlayerPreferences.MAX_PLAYER_CACHE_SIZE_MB,
                        stepSize = PlayerPreferences.PLAYER_CACHE_SIZE_STEP_MB,
                        onValueChanged = viewModel::setPlayerCacheSizeMb,
                        valueLabel = { sizeMb ->
                            stringResource(R.string.player_settings_player_cache_size_value, sizeMb)
                        },
                        defaultLabel = { sizeMb ->
                            stringResource(
                                R.string.player_settings_default_value,
                                stringResource(R.string.player_settings_player_cache_size_value, sizeMb)
                            )
                        },
                        accentColor = cacheColor
                    )

                    SettingsDivider()
                    ValueSliderSettingsItem(
                        icon = Icons.Rounded.Schedule,
                        title = stringResource(R.string.player_settings_player_cache_time),
                        subtitle = stringResource(R.string.player_settings_player_cache_time_summary),
                        value = uiState.playerCacheTimeSeconds,
                        defaultValue = PlayerPreferences.DEFAULT_PLAYER_CACHE_TIME_SECONDS,
                        minValue = PlayerPreferences.MIN_PLAYER_CACHE_TIME_SECONDS,
                        maxValue = PlayerPreferences.MAX_PLAYER_CACHE_TIME_SECONDS,
                        stepSize = PlayerPreferences.PLAYER_CACHE_TIME_STEP_SECONDS,
                        onValueChanged = viewModel::setPlayerCacheTimeSeconds,
                        valueLabel = { seconds ->
                            stringResource(R.string.player_settings_player_cache_time_value, seconds)
                        },
                        defaultLabel = { seconds ->
                            stringResource(
                                R.string.player_settings_default_value,
                                stringResource(R.string.player_settings_player_cache_time_value, seconds)
                            )
                        },
                        accentColor = cacheColor
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.player_settings_section_performance)) }
            item {
                SettingsSection {
                    SwitchSettingsItem(
                        icon = Icons.Rounded.BatteryStd,
                        title = stringResource(R.string.player_settings_battery_optimization),
                        subtitle = stringResource(R.string.player_settings_battery_optimization_summary),
                        checked = uiState.batteryOptimizationEnabled,
                        onCheckedChange = viewModel::setBatteryOptimizationEnabled,
                        accentColor = performanceColor
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleSettingsScreen(
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val playerPreferences = remember { PlayerPreferences(context) }
    val subtitleAccent = Color(0xFF6366F1)
    val positionAccent = Color(0xFF0EA5E9)

    var textSize by remember { mutableStateOf(playerPreferences.getSubtitleTextSize()) }
    var textColor by remember { mutableStateOf(playerPreferences.getSubtitleTextColor()) }
    var backgroundColor by remember { mutableStateOf(playerPreferences.getSubtitleBackgroundColor()) }
    var edgeType by remember { mutableStateOf(playerPreferences.getSubtitleEdgeType()) }
    var textOpacityPercent by remember { mutableStateOf(playerPreferences.getSubtitleTextOpacityPercent()) }
    var bottomEdgePercent by remember {
        mutableStateOf(playerPreferences.getSubtitlePosition())
    }
    var topEdgePercent by remember {
        mutableStateOf(playerPreferences.getSubtitleTopEdgePositionPercent())
    }

    Scaffold(
        topBar = {
            topbar(title = stringResource(R.string.subtitle_settings_title), onBackPressed = onBackPressed)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item { SectionLabel(stringResource(R.string.subtitle_settings_section_style)) }
            item {
                SettingsSection {
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.Tune,
                        title = stringResource(R.string.subtitle_settings_text_size),
                        subtitle = subtitleTextSizeLabel(textSize),
                        selectedValue = textSize,
                        options = listOf(
                            SelectionOption("Small", "Pequeño", "Texto compacto, más espacio en pantalla"),
                            SelectionOption("Normal", "Normal", "Tamaño estándar y legible", isDefault = true),
                            SelectionOption("Large", "Grande", "Texto más grande y fácil de leer"),
                            SelectionOption("Extra Large", "Muy grande", "Máxima legibilidad")
                        ),
                        onOptionSelected = { selected ->
                            textSize = selected
                            playerPreferences.setSubtitleTextSize(selected)
                        },
                        accentColor = subtitleAccent
                    )

                    SettingsDivider()
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.SortByAlpha,
                        title = stringResource(R.string.subtitle_settings_text_color),
                        subtitle = subtitleTextColorLabel(textColor),
                        selectedValue = textColor,
                        options = listOf(
                            SelectionOption("White", "Blanco", "Estándar, funciona en la mayoría de fondos", isDefault = true),
                            SelectionOption("Yellow", "Amarillo", "Alto contraste en escenas oscuras"),
                            SelectionOption("Green", "Verde", "Color distintivo para facilitar la visibilidad"),
                            SelectionOption("Cyan", "Cian", "Tono frío, cómodo para la vista")
                        ),
                        onOptionSelected = { selected ->
                            textColor = selected
                            playerPreferences.setSubtitleTextColor(selected)
                        },
                        accentColor = subtitleAccent
                    )

                    SettingsDivider()
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.Brush,
                        title = stringResource(R.string.subtitle_settings_background_color),
                        subtitle = subtitleBackgroundLabel(backgroundColor),
                        selectedValue = backgroundColor,
                        options = listOf(
                            SelectionOption("Transparent", "Transparente", "Sin fondo, aspecto limpio", isDefault = true),
                            SelectionOption("Black", "Negro", "Recuadro oscuro detrás del texto para aumentar el contraste"),
                            SelectionOption("White", "Blanco", "Recuadro claro detrás del texto")
                        ),
                        onOptionSelected = { selected ->
                            backgroundColor = selected
                            playerPreferences.setSubtitleBackgroundColor(selected)
                        },
                        accentColor = subtitleAccent
                    )

                    SettingsDivider()
                    SelectionDialogSettingsItem(
                        icon = Icons.Rounded.Tune,
                        title = stringResource(R.string.subtitle_settings_edge_type),
                        subtitle = subtitleEdgeLabel(edgeType),
                        selectedValue = edgeType,
                        options = listOf(
                            SelectionOption("None", "Ninguno", "Sin efecto de borde", isDefault = true),
                            SelectionOption("Outline", "Contorno", "Borde alrededor del texto para facilitar la lectura"),
                            SelectionOption("Drop Shadow", "Sombra paralela", "Sombra bajo el texto para dar profundidad"),
                            SelectionOption("Raised", "Elevado", "Efecto 3D en relieve"),
                            SelectionOption("Depressed", "Hundido", "Efecto 3D grabado")
                        ),
                        onOptionSelected = { selected ->
                            edgeType = selected
                            playerPreferences.setSubtitleEdgeType(selected)
                        },
                        accentColor = subtitleAccent
                    )

                    SettingsDivider()
                    PercentageSliderSettingsItem(
                        icon = Icons.Rounded.SortByAlpha,
                        title = stringResource(R.string.subtitle_settings_text_opacity),
                        subtitle = stringResource(R.string.subtitle_settings_text_opacity_summary),
                        value = textOpacityPercent,
                        defaultValue = PlayerPreferences.DEFAULT_SUBTITLE_TEXT_OPACITY_PERCENT,
                        minValue = 0,
                        maxValue = 100,
                        onValueChanged = { updated ->
                            textOpacityPercent = updated
                            playerPreferences.setSubtitleTextOpacityPercent(updated)
                        },
                        accentColor = subtitleAccent
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.subtitle_settings_section_position)) }
            item {
                SettingsSection {
                    PercentageSliderSettingsItem(
                        icon = Icons.Rounded.Fullscreen,
                        title = stringResource(R.string.subtitle_settings_bottom_edge_position),
                        subtitle = stringResource(R.string.subtitle_settings_bottom_edge_position_summary),
                        value = bottomEdgePercent,
                        defaultValue = PlayerPreferences.DEFAULT_SUBTITLE_BOTTOM_EDGE_PERCENT,
                        onValueChanged = { updated ->
                            bottomEdgePercent = updated
                            playerPreferences.setSubtitleBottomEdgePositionPercent(updated)
                        },
                        accentColor = positionAccent
                    )

                    SettingsDivider()
                    PercentageSliderSettingsItem(
                        icon = Icons.Rounded.Fullscreen,
                        title = stringResource(R.string.subtitle_settings_top_edge_position),
                        subtitle = stringResource(R.string.subtitle_settings_top_edge_position_summary),
                        value = topEdgePercent,
                        defaultValue = PlayerPreferences.DEFAULT_SUBTITLE_TOP_EDGE_PERCENT,
                        onValueChanged = { updated ->
                            topEdgePercent = updated
                            playerPreferences.setSubtitleTopEdgePositionPercent(updated)
                        },
                        accentColor = positionAccent
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topbar(
    title: String,
    onBackPressed: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun SwitchSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    BaseSettingsItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        accentColor = accentColor,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    checkedBorderColor = accentColor,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    disabledCheckedTrackColor = accentColor.copy(alpha = 0.45f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )
        }
    )
}

@Composable
private fun ClickableSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    BaseSettingsItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        isDestructive = isDestructive,
        accentColor = accentColor,
        onClick = if (enabled) onClick else null,
        trailing = {
            if (enabled) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}


@Composable
private fun BaseSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null && enabled) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = when {
                        isDestructive -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        else -> accentColor.copy(alpha = 0.16f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when {
                    isDestructive -> MaterialTheme.colorScheme.error
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else -> accentColor
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    isDestructive -> MaterialTheme.colorScheme.error
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        trailing?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValueSliderSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: Int,
    defaultValue: Int,
    minValue: Int,
    maxValue: Int,
    stepSize: Int = 1,
    onValueChanged: (Int) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    valueLabel: @Composable (Int) -> String,
    defaultLabel: @Composable (Int) -> String
) {
    val safeMin = minValue
    val safeMax = maxValue.coerceAtLeast(minValue + 1)
    val safeStepSize = stepSize.coerceAtLeast(1)
    val valueRange = safeMin..safeMax
    val sliderSteps = (((safeMax - safeMin) / safeStepSize) - 1).coerceAtLeast(0)
    val safeValue = value.coerceIn(valueRange.first, valueRange.last)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                text = valueLabel(safeValue),
                style = MaterialTheme.typography.titleSmall,
                color = accentColor
            )
        }

        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            value = safeValue.toFloat(),
            onValueChange = { changed ->
                val steppedValue = (
                    ((changed - safeMin) / safeStepSize).roundToInt() * safeStepSize + safeMin
                ).coerceIn(valueRange.first, valueRange.last)
                onValueChanged(steppedValue)
            },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = sliderSteps,
            accentColor = accentColor,
            inactiveTrackColor = accentColor.copy(alpha = 0.25f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = valueLabel(valueRange.first),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = defaultLabel(defaultValue),
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                modifier = Modifier.clickable {
                    onValueChanged(defaultValue.coerceIn(valueRange.first, valueRange.last))
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = valueLabel(valueRange.last),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PercentageSliderSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: Int,
    defaultValue: Int,
    minValue: Int = 0,
    maxValue: Int = 50,
    stepSize: Int = 5,
    onValueChanged: (Int) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    ValueSliderSettingsItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        value = value,
        defaultValue = defaultValue,
        minValue = minValue,
        maxValue = maxValue,
        stepSize = stepSize,
        onValueChanged = onValueChanged,
        accentColor = accentColor,
        valueLabel = { currentValue ->
            stringResource(R.string.player_settings_percent_value, currentValue)
        },
        defaultLabel = { currentValue ->
            stringResource(R.string.player_settings_default_percent, currentValue)
        }
    )
}


@Composable
private fun SelectionDialogSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selectedValue: String,
    options: List<SelectionOption>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    var showDialog by remember { mutableStateOf(false) }

    BaseSettingsItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        accentColor = accentColor,
        onClick = { showDialog = true },
        trailing = {
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    )

    if (showDialog) {
        AmoledSelectionDialog(
            title = title,
            options = options,
            selectedValue = selectedValue,
            accentColor = accentColor,
            onOptionSelected = { value ->
                onOptionSelected(value)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerSettingsScreenPreview() {
    PlayerSettingsScreen()
}

private fun playerEngineLabel(value: String): String = when (value) {
    "ExoPlayer" -> "ExoPlayer"
    "MPV" -> "MPV"
    else -> value
}

private fun hardwareDecodingLabel(value: String): String = when (value) {
    "mediacodec" -> "MediaCodec"
    "mediacodec-copy" -> "MediaCodec (copia)"
    "no" -> "Software"
    else -> value
}

private fun videoOutputLabel(value: String): String = when (value) {
    "gpu-next" -> "GPU Next"
    "gpu" -> "GPU"
    else -> value
}

private fun audioOutputLabel(value: String): String = when (value) {
    "audiotrack" -> "AudioTrack"
    "aaudio" -> "AAudio"
    "opensles" -> "OpenSL ES"
    else -> value
}

private fun upscaleFilterLabel(value: String): String = when (value) {
    "bilinear" -> "Bilinear"
    "spline36" -> "Spline36"
    "lanczos" -> "Lanczos"
    "ewa_lanczos" -> "EWA Lanczos"
    else -> value
}

private fun downscaleFilterLabel(value: String): String = when (value) {
    "hermite" -> "Hermite"
    "mitchell" -> "Mitchell"
    "catmull_rom" -> "Catmull-Rom"
    "lanczos" -> "Lanczos"
    else -> value
}

private fun toneMappingLabel(value: String): String = when (value) {
    "auto" -> "Auto"
    "bt.2390" -> "BT.2390"
    "spline" -> "Spline"
    "hable" -> "Filmic"
    "mobius" -> "Mobius"
    "reinhard" -> "Reinhard"
    else -> value
}

private fun audioTranscodeModeLabel(value: String): String = when (value) {
    "Auto" -> "Auto"
    "Stereo" -> "Estéreo"
    "5.1 Surround" -> "Sonido envolvente 5.1"
    "Passthrough" -> "Transferencia directa"
    else -> value
}

private fun subtitleTextSizeLabel(value: String): String = when (value) {
    "Small" -> "Pequeño"
    "Normal" -> "Normal"
    "Large" -> "Grande"
    "Extra Large" -> "Muy grande"
    else -> value
}

private fun subtitleTextColorLabel(value: String): String = when (value) {
    "White" -> "Blanco"
    "Yellow" -> "Amarillo"
    "Green" -> "Verde"
    "Cyan" -> "Cian"
    else -> value
}

private fun subtitleBackgroundLabel(value: String): String = when (value) {
    "Transparent" -> "Transparente"
    "Black" -> "Negro"
    "White" -> "Blanco"
    else -> value
}

private fun subtitleEdgeLabel(value: String): String = when (value) {
    "None" -> "Ninguno"
    "Outline" -> "Contorno"
    "Drop Shadow" -> "Sombra paralela"
    "Raised" -> "Elevado"
    "Depressed" -> "Hundido"
    else -> value
}

@Composable
private fun decoderPriorityLabel(value: String): String {
    return when (value) {
        PlayerPreferences.DECODER_PRIORITY_HARDWARE -> stringResource(R.string.player_settings_decoder_priority_hardware_first)
        PlayerPreferences.DECODER_PRIORITY_SOFTWARE -> stringResource(R.string.player_settings_decoder_priority_software_first)
        else -> stringResource(R.string.settings_auto)
    }
}
