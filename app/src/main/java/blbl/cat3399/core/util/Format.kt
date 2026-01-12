package blbl.cat3399.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Format {
    fun duration(sec: Int): String {
        val s = if (sec < 0) 0 else sec
        val h = s / 3600
        val m = (s % 3600) / 60
        val ss = s % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, ss)
        else String.format(Locale.US, "%02d:%02d", m, ss)
    }

    fun count(n: Long?): String {
        val v = n ?: return "-"
        return when {
            v >= 100_000_000 -> String.format(Locale.US, "%.1f亿", v / 100_000_000.0)
            v >= 10_000 -> String.format(Locale.US, "%.1f万", v / 10_000.0)
            else -> v.toString()
        }
    }

    fun timeText(epochSec: Long, nowMs: Long = System.currentTimeMillis()): String {
        if (epochSec <= 0) return "-"
        val whenMs = epochSec * 1000

        val now = Calendar.getInstance().apply { timeInMillis = nowMs }
        val then = Calendar.getInstance().apply { timeInMillis = whenMs }

        val sameDay =
            now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

        return if (sameDay) {
            val sdf = SimpleDateFormat("HH:mm", Locale.CHINA)
            "今天 ${sdf.format(Date(whenMs))}"
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            sdf.format(Date(whenMs))
        }
    }

    fun pubDateText(epochSec: Long, nowMs: Long = System.currentTimeMillis()): String {
        if (epochSec <= 0) return ""
        val whenMs = epochSec * 1000
        val diffMs = nowMs - whenMs
        if (diffMs < 0) {
            val sdf = SimpleDateFormat("yyyy.M.d", Locale.CHINA)
            return sdf.format(Date(whenMs))
        }

        val minuteMs = 60_000L
        val hourMs = 3_600_000L
        val dayMs = 86_400_000L

        return when {
            diffMs < minuteMs -> {
                val sec = maxOf(1, diffMs / 1000)
                "${sec}秒前"
            }
            diffMs < hourMs -> {
                val min = maxOf(1, diffMs / minuteMs)
                "${min}分钟前"
            }
            diffMs < dayMs -> {
                val hour = maxOf(1, diffMs / hourMs)
                "${hour}小时前"
            }
            diffMs < 3 * dayMs -> {
                val day = maxOf(1, diffMs / dayMs)
                "${day}天前"
            }
            else -> {
                val sdf = SimpleDateFormat("yyyy.M.d", Locale.CHINA)
                sdf.format(Date(whenMs))
            }
        }
    }
}
