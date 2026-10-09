package com.peakvalle.hours.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.time.Duration

private val locale: Locale get() = Locale.getDefault()

/** Cuenta regresiva: `02:13:45` si hay horas, `13:45` si no. */
fun Duration.toCountdown(): String {
    val total = inWholeSeconds.coerceAtLeast(0L)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) {
        String.format(locale, "%02d:%02d:%02d", h, m, s)
    } else {
        String.format(locale, "%02d:%02d", m, s)
    }
}

/** Duración legible: `2d 7h`, `7h 15m` o `45m`. */
fun Duration.toCoarse(): String {
    val total = inWholeMinutes.coerceAtLeast(0L)
    val days = total / (60 * 24)
    val h = (total % (60 * 24)) / 60
    val m = total % 60
    return when {
        days > 0 -> String.format(locale, "%dd %dh", days, h)
        h > 0 -> String.format(locale, "%dh %02dm", h, m)
        else -> String.format(locale, "%dm", m)
    }
}

/** Hora local en formato `HH:mm` (o `HH:mm:ss`). */
fun Instant.formatLocalTime(zone: TimeZone, withSeconds: Boolean = false): String {
    val time = toLocalDateTime(zone)
    return if (withSeconds) {
        String.format(locale, "%02d:%02d:%02d", time.hour, time.minute, time.second)
    } else {
        String.format(locale, "%02d:%02d", time.hour, time.minute)
    }
}

/** Rango local: `01:00 – 04:00`. */
fun formatLocalRange(start: Instant, end: Instant, zone: TimeZone): String =
    "${start.formatLocalTime(zone)} – ${end.formatLocalTime(zone)}"