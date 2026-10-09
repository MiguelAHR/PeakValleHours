package com.peakvalle.hours.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Estado calculado de la tarifa en un instante [now] concreto.
 *
 * @property window ventana actual (la que contiene a [now]).
 * @property next siguiente ventana (con estado distinto), o `null` si no aplica.
 */
data class CurrentRate(
    val status: RateStatus,
    val window: RateWindow,
    val next: RateWindow?,
    val now: Instant
) {
    val isPeak: Boolean get() = status == RateStatus.PEAK

    /** Tiempo que queda hasta el próximo cambio de tarifa. */
    val remaining: Duration get() = window.end - now

    /** Tiempo transcurrido dentro de la ventana actual. */
    val elapsed: Duration get() = now - window.start

    /** Duración total de la ventana actual. */
    val total: Duration get() = window.duration

    /** Progreso de la ventana actual en [0f, 1f]. */
    val progress: Float
        get() {
            val totalMs = total.inWholeMilliseconds
            if (totalMs <= 0L) return 1f
            return (elapsed.inWholeMilliseconds.toDouble() / totalMs.toDouble())
                .toFloat()
                .coerceIn(0f, 1f)
        }
}
