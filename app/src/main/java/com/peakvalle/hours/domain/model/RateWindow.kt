package com.peakvalle.hours.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Ventana de tiempo continua con una [status] fija dentro de [start, end).
 */
data class RateWindow(
    val status: RateStatus,
    val start: Instant,
    val end: Instant
) {
    val duration: Duration get() = end - start

    fun contains(instant: Instant): Boolean = instant >= start && instant < end
}
