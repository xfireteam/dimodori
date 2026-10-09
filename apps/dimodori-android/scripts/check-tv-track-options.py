#!/usr/bin/env python3
"""Compile existing production mapping/model on JVM, omitting serialization only.

The DTO and functions are extracted at runtime, not mocked/reimplemented.
This is not an Android or serialization compiler check.
"""
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
DETAIL = ROOT / "phone/src/main/java/com/dimodori/app/tv/ui/screens/detail"

with tempfile.TemporaryDirectory(prefix="dimodori-track-options-") as folder:
    tmp = Path(folder)
    model_source = (ROOT / "data/src/commonMain/com/jellycine/data/model/PlaybackInfoResponse.kt").read_text()
    start = model_source.index("data class MediaStream(")
    end = model_source.index("\n)", start) + len("\n)")
    model = re.sub(r'@SerialName\("[^"]+"\)\s*', "", model_source[start:end])
    model_file = tmp / "MediaStream.kt"
    model_file.write_text("package com.jellycine.data.model\n" + model + "\n")
    tracks = (DETAIL / "TrackSection.kt").read_text()
    functions = tracks[tracks.index("internal fun buildVideoOptions("):]
    options_file = tmp / "TrackOptions.kt"
    options_file.write_text(
        "package com.dimodori.app.tv.ui.screens.detail\n"
        "import com.jellycine.data.model.MediaStream\n"
        "import com.jellycine.player.core.mediaStreamDisplayTitles\n"
        "import com.jellycine.player.core.defaultSubtitleDisplayTitle\n"
        'private const val SUBTITLE_OFF_SENTINEL = "Off"\n' + functions
    )
    jar = tmp / "tracks.jar"
    subprocess.run([
        "kotlinc", str(model_file), str(options_file),
        str(ROOT / "core/src/main/java/com/jellycine/player/core/MediaStreamDisplayTitles.kt"),
        str(ROOT / "scripts/TrackOptionChecks.kt"),
        "-include-runtime", "-d", str(jar),
    ], check=True)
    subprocess.run(["java", "-jar", str(jar)], check=True)
