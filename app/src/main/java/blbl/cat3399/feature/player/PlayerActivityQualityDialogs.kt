package blbl.cat3399.feature.player

import blbl.cat3399.core.ui.SingleChoiceDialog
import java.util.Locale

internal fun PlayerActivity.showCodecDialog() {
    val options = arrayOf("AVC", "HEVC", "AV1")
    val current = options.indexOf(session.preferCodec).coerceAtLeast(0)
    SingleChoiceDialog.show(
        context = this,
        title = "视频编码",
        items = options.toList(),
        checkedIndex = current,
        negativeText = "取消",
    ) { which, _ ->
        val selected = options.getOrNull(which) ?: "AVC"
        session = session.copy(preferCodec = selected)
        refreshSettings(binding.recyclerSettings.adapter as PlayerSettingsAdapter)
        reloadStream(keepPosition = true)
    }
}

internal fun PlayerActivity.showSpeedDialog() {
    val options = arrayOf("0.50x", "0.75x", "1.00x", "1.25x", "1.50x", "2.00x")
    val current = options.indexOf(String.format(Locale.US, "%.2fx", session.playbackSpeed)).let { if (it >= 0) it else 2 }
    SingleChoiceDialog.show(
        context = this,
        title = "播放速度",
        items = options.toList(),
        checkedIndex = current,
        negativeText = "取消",
    ) { which, _ ->
        val selected = options.getOrNull(which) ?: "1.00x"
        val v = selected.removeSuffix("x").toFloatOrNull() ?: 1.0f
        session = session.copy(playbackSpeed = v)
        player?.setPlaybackSpeed(v)
        refreshSettings(binding.recyclerSettings.adapter as PlayerSettingsAdapter)
    }
}

internal fun PlayerActivity.showResolutionDialog() {
    // Follow docs: qn list for resolution/framerate.
    // Keep the full list so user can force-pick even if the server later falls back.
    val docQns = listOf(16, 32, 64, 74, 80, 100, 112, 116, 120, 125, 126, 127, 129)
    val available = lastAvailableQns.toSet()
    val options =
        docQns.map { qn ->
            val label = qnLabel(qn)
            if (available.contains(qn)) "${label}（可用）" else label
        }

    val currentQn =
        session.actualQn.takeIf { it > 0 }
            ?: session.targetQn.takeIf { it > 0 }
            ?: session.preferredQn
    val currentIndex = docQns.indexOfFirst { it == currentQn }.takeIf { it >= 0 } ?: 0
    SingleChoiceDialog.show(
        context = this,
        title = "分辨率",
        items = options,
        checkedIndex = currentIndex,
        neutralText = "自动",
        onNeutral = {
            session = session.copy(targetQn = 0)
            refreshSettings(binding.recyclerSettings.adapter as PlayerSettingsAdapter)
            reloadStream(keepPosition = true)
        },
        negativeText = "取消",
    ) { which, _ ->
        val qn = docQns.getOrNull(which) ?: return@show
        session = session.copy(targetQn = qn)
        refreshSettings(binding.recyclerSettings.adapter as PlayerSettingsAdapter)
        reloadStream(keepPosition = true)
    }
}

internal fun PlayerActivity.showAudioDialog() {
    val docIds = listOf(30251, 30250, 30280, 30232, 30216)
    val available = lastAvailableAudioIds.toSet()
    val options =
        docIds.map { id ->
            val label = audioLabel(id)
            if (available.contains(id)) "${label}（可用）" else label
        }

    val currentId =
        session.actualAudioId.takeIf { it > 0 }
            ?: session.targetAudioId.takeIf { it > 0 }
            ?: session.preferAudioId
    val currentIndex = docIds.indexOfFirst { it == currentId }.takeIf { it >= 0 } ?: 0

    SingleChoiceDialog.show(
        context = this,
        title = "音轨",
        items = options,
        checkedIndex = currentIndex,
        neutralText = "默认",
        onNeutral = {
            session = session.copy(targetAudioId = 0)
            refreshSettings(binding.recyclerSettings.adapter as PlayerSettingsAdapter)
            reloadStream(keepPosition = true)
        },
        negativeText = "取消",
    ) { which, _ ->
        val id = docIds.getOrNull(which) ?: return@show
        session = session.copy(targetAudioId = id)
        refreshSettings(binding.recyclerSettings.adapter as PlayerSettingsAdapter)
        reloadStream(keepPosition = true)
    }
}

