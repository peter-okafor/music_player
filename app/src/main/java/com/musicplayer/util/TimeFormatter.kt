package com.musicplayer.util

import java.util.concurrent.TimeUnit

object TimeFormatter {

    /** mm:ss, or h:mm:ss for anything over an hour. */
    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:00"

        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }

    /** "1 hr 12 min" style summary used for album / playlist totals. */
    fun formatTotal(durationMs: Long): String {
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "$hours hr $minutes min"
            hours > 0 -> "$hours hr"
            else -> "$minutes min"
        }
    }

    /** Countdown display for the sleep timer. */
    fun formatCountdown(remainingMs: Long): String {
        val safe = remainingMs.coerceAtLeast(0)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(safe)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(safe) % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
