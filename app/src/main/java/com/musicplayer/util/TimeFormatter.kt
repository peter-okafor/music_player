package com.musicplayer.util

import java.util.concurrent.TimeUnit

object TimeFormatter {
    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:00"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

        return "%d:%02d".format(minutes, seconds)
    }
}
