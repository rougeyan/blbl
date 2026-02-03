package blbl.cat3399.feature.player

import android.widget.Toast
import blbl.cat3399.core.api.BiliApiException
import blbl.cat3399.core.net.BiliClient
import org.json.JSONObject

internal fun PlayerActivity.handlePlayUrlErrorIfNeeded(t: Throwable): Boolean {
    val e = t as? BiliApiException ?: return false
    if (!isRiskControl(e)) return false

    val prefs = BiliClient.prefs
    val savedVoucher = prefs.gaiaVgateVVoucher
    val savedAt = prefs.gaiaVgateVVoucherSavedAtMs
    val hasSavedVoucher = !savedVoucher.isNullOrBlank()

    // Keep this non-blocking: never show modal dialogs or auto-jump away from playback.
    // Users can choose to go to Settings manually if needed.
    val msg =
        buildString {
            append("B 站返回：").append(e.apiCode).append(" / ").append(e.apiMessage)
            if (e.apiCode == -352 && hasSavedVoucher) {
                append("\n")
                append("已记录 v_voucher，可到“设置 -> 风控验证”手动完成人机验证后重试。")
                if (savedAt > 0L) {
                    append("\n")
                    append("记录时间：").append(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", savedAt))
                }
            } else {
                append("\n")
                append("可能触发风控，建议重新登录或稍后重试。")
            }
        }
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    return true
}

internal fun PlayerActivity.showRiskControlBypassHintIfNeeded(playJson: JSONObject) {
    if (riskControlBypassHintShown) return
    if (!playJson.optBoolean("__blbl_risk_control_bypassed", false)) return
    riskControlBypassHintShown = true

    val code = playJson.optInt("__blbl_risk_control_code", 0)
    val message = playJson.optString("__blbl_risk_control_message", "")
    val msg =
        buildString {
            append("B 站返回：").append(code).append(" / ").append(message)
            append("\n\n")
            append("你的账号或网络环境可能触发风控，建议重新登录或稍后重试。")
            append("\n")
            append("如持续出现，请向作者反馈日志。")
        }
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

internal fun PlayerActivity.isRiskControl(e: BiliApiException): Boolean {
    if (e.apiCode == -412 || e.apiCode == -352) return true
    val m = e.apiMessage
    return m.contains("风控") || m.contains("拦截") || m.contains("风险")
}
