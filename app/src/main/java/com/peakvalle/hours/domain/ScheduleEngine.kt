package com.peakvalle.hours.domain

import com.peakvalle.hours.domain.model.CurrentRate
import com.peakvalle.hours.domain.model.RateStatus
import com.peakvalle.hours.domain.model.RateWindow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

/**
 * Motor de cálculo del horario PEAK / VALLE de DeepSeek.
 *
 * Reglas oficiales (siempre en UTC):
 *  - PEAK: 01:00–04:00 y 06:00–10:00, de lunes a viernes.
 *  - VALLE (OFF_PEAK): el resto del tiempo, incluido todo el fin de semana.
 *
 * El motor trabaja en UTC y solo convierte a local en la capa de presentación.
 */
class ScheduleEngine(
    private val zone: TimeZone = TimeZone.UTC
) {
    fun isPeak(instant: Instant): Boolean = currentRate(instant).isPeak

    /** Instante del próximo cambio de tarifa a partir de [instant]. */
    fun nextTransition(instant: Instant): Instant = currentRate(instant).window.end

    /**
     * Ventanas que se solapan con el rango `[from, until)`.
     *
     * Se usan para dibujar la agenda de las próximas horas. Las ventanas de
     * los extremos se recortan a `[from, until)` para poder pintarlas como
     * bloques parciales sin salirse del rango.
     */
    fun windowsBetween(from: Instant, until: Instant): List<RateWindow> =
        buildTimeline(from)
            .filter { it.end > from && it.start < until }
            .map { window ->
                window.copy(
                    start = if (window.start < from) from else window.start,
                    end = if (window.end > until) until else window.end
                )
            }

    /**
     * Calcula el estado actual y las ventanas relevantes alrededor de [now].
     */
    fun currentRate(now: Instant): CurrentRate {
        val segments = buildTimeline(now)
        val index = segments.indexOfFirst { it.contains(now) }
        require(index != -1) { "No se encontró ventana para $now en $zone" }
        val current = segments[index]
        val next = segments.getOrNull(index + 1)
        return CurrentRate(
            status = current.status,
            window = current,
            next = next,
            now = now
        )
    }

    private fun buildTimeline(now: Instant): List<RateWindow> {
        val today = now.toLocalDateTime(zone).date
        val raw = ArrayList<RateWindow>()
        var day: LocalDate = today.minus(1, DateTimeUnit.DAY)
        repeat(TIMELINE_DAYS) {
            raw += windowsForDate(day)
            day = day.plus(1, DateTimeUnit.DAY)
        }
        return mergeAdjacent(raw)
    }

    private fun windowsForDate(date: LocalDate): List<RateWindow> {
        val dayStart = date.atStartOfDayIn(zone)
        val dayEnd = dayStart + 24.hours

        if (date.dayOfWeek !in WEEKDAYS) {
            return listOf(RateWindow(RateStatus.OFF_PEAK, dayStart, dayEnd))
        }

        val result = ArrayList<RateWindow>()
        var cursor = dayStart
        for ((startHour, endHour) in PEAK_BLOCKS_UTC) {
            val peakStart = dayStart + startHour.hours
            val peakEnd = dayStart + endHour.hours
            if (cursor < peakStart) {
                result += RateWindow(RateStatus.OFF_PEAK, cursor, peakStart)
            }
            result += RateWindow(RateStatus.PEAK, peakStart, peakEnd)
            cursor = peakEnd
        }
        if (cursor < dayEnd) {
            result += RateWindow(RateStatus.OFF_PEAK, cursor, dayEnd)
        }
        return result
    }

    /** Une ventanas contiguas con el mismo estado (p. ej. todo el fin de semana). */
    private fun mergeAdjacent(segments: List<RateWindow>): List<RateWindow> {
        val merged = ArrayList<RateWindow>()
        for (segment in segments) {
            val last = merged.lastOrNull()
            if (last != null && last.status == segment.status && last.end == segment.start) {
                merged[merged.size - 1] = last.copy(end = segment.end)
            } else {
                merged += segment
            }
        }
        return merged
    }

    companion object {
        /**
         * Días generados alrededor de [now] (hoy−1 … hoy+3).
         *
         * El rango debe ser lo bastante amplio para que la ventana actual nunca
         * quede truncada en el borde: el fin de semana completo abarca unos
         * 3 días, así que un rango corto reportaría un `window.end` incorrecto.
         */
        private const val TIMELINE_DAYS = 5

        /** Bloques PEAK expresados en UTC como [horaInicio, horaFin). */
        val PEAK_BLOCKS_UTC: List<Pair<Int, Int>> = listOf(1 to 4, 6 to 10)

        private val WEEKDAYS = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    }
}
