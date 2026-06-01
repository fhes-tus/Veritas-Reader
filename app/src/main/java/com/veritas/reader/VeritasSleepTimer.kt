package com.veritas.reader

import kotlin.math.ceil

enum class VeritasSleepTimerAction(val label: String) {
    PAUSE("Pause playback"),
    STOP("Stop playback");

    companion object {
        fun fromName(value: String?): VeritasSleepTimerAction =
            entries.firstOrNull { it.name == value } ?: PAUSE
    }
}

data class VeritasSleepTimerRequest(
    val durationMillis: Long,
    val action: VeritasSleepTimerAction
) {
    init {
        require(durationMillis in MIN_DURATION_MILLIS..MAX_DURATION_MILLIS) {
            "Sleep timer duration must be between 1 minute and 12 hours."
        }
    }

    fun endsAt(nowMillis: Long = System.currentTimeMillis()): Long =
        nowMillis + durationMillis

    companion object {
        const val MIN_DURATION_MILLIS: Long = 60_000L
        const val MAX_DURATION_MILLIS: Long = 12L * 60L * 60L * 1000L
    }
}

data class VeritasSleepTimerSnapshot(
    val durationMillis: Long,
    val endsAtMillis: Long,
    val action: VeritasSleepTimerAction
) {
    fun remainingMillis(nowMillis: Long = System.currentTimeMillis()): Long =
        (endsAtMillis - nowMillis).coerceAtLeast(0L)

    fun isActive(nowMillis: Long = System.currentTimeMillis()): Boolean =
        remainingMillis(nowMillis) > 0L

    fun menuLabel(nowMillis: Long = System.currentTimeMillis()): String =
        "Sleep timer • ${VeritasSleepTimerFormatter.formatRemaining(remainingMillis(nowMillis))}"
}

object VeritasSleepTimerPresets {
    val durationsMillis: List<Long> = listOf(5L, 10L, 15L, 30L, 45L, 60L)
        .map { it * 60L * 1000L }
}

object VeritasSleepTimerFormatter {
    fun formatDuration(durationMillis: Long): String {
        val totalMinutes = ceil(durationMillis.coerceAtLeast(0L) / 60_000.0).toLong()
        if (totalMinutes < 60L) return "$totalMinutes min"
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return if (minutes == 0L) "${hours}h" else "${hours}h ${minutes}m"
    }

    fun formatRemaining(remainingMillis: Long): String {
        val totalSeconds = ceil(remainingMillis.coerceAtLeast(0L) / 1000.0).toLong()
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return if (minutes >= 60L) {
            formatDuration(remainingMillis)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }
}
