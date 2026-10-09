package com.peakvalle.hours.domain

import com.peakvalle.hours.domain.model.RateStatus
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Semana de referencia (UTC):
 *   Lun 2026-01-05  ...  Dom 2026-01-11
 */
class ScheduleEngineTest {

    private val engine = ScheduleEngine()

    private fun at(iso: String): Instant = Instant.parse(iso)

    @Test
    fun `lunes dentro del bloque 01-04 es PEAK`() {
        val rate = engine.currentRate(at("2026-01-05T02:00:00Z"))
        assertEquals(RateStatus.PEAK, rate.status)
        assertTrue(rate.isPeak)
        assertEquals(at("2026-01-05T04:00:00Z"), rate.window.end)
        assertEquals(2.hours, rate.remaining)
    }

    @Test
    fun `lunes en el hueco 04-06 es VALLE y el siguiente es PEAK`() {
        val rate = engine.currentRate(at("2026-01-05T05:00:00Z"))
        assertEquals(RateStatus.OFF_PEAK, rate.status)
        assertEquals(1.hours, rate.remaining)
        assertEquals(RateStatus.PEAK, rate.next?.status)
        assertEquals(at("2026-01-05T06:00:00Z"), rate.next?.start)
    }

    @Test
    fun `lunes 10-00 abre VALLE largo hasta el martes 01-00`() {
        val rate = engine.currentRate(at("2026-01-05T10:00:00Z"))
        assertEquals(RateStatus.OFF_PEAK, rate.status)
        assertEquals(15.hours, rate.remaining)
        assertEquals(RateStatus.PEAK, rate.next?.status)
        assertEquals(at("2026-01-06T01:00:00Z"), rate.next?.start)
    }

    @Test
    fun `lunes 00-30 es VALLE faltando 30 minutos`() {
        val rate = engine.currentRate(at("2026-01-05T00:30:00Z"))
        assertEquals(RateStatus.OFF_PEAK, rate.status)
        assertEquals(30.minutes, rate.remaining)
        assertEquals(at("2026-01-05T01:00:00Z"), rate.window.end)
    }

    @Test
    fun `viernes de madrugada sigue siendo PEAK`() {
        val rate = engine.currentRate(at("2026-01-09T02:00:00Z"))
        assertEquals(RateStatus.PEAK, rate.status)
    }

    @Test
    fun `sabado es VALLE y dura hasta el lunes 01-00`() {
        val rate = engine.currentRate(at("2026-01-10T02:00:00Z"))
        assertEquals(RateStatus.OFF_PEAK, rate.status)
        assertEquals(47.hours, rate.remaining)
        assertEquals(at("2026-01-12T01:00:00Z"), rate.window.end)
        assertEquals(RateStatus.PEAK, rate.next?.status)
    }

    @Test
    fun `domingo 23-00 cierra el fin de semana faltando 2 horas`() {
        val rate = engine.currentRate(at("2026-01-11T23:00:00Z"))
        assertEquals(RateStatus.OFF_PEAK, rate.status)
        assertEquals(2.hours, rate.remaining)
        assertEquals(at("2026-01-12T01:00:00Z"), rate.window.end)
    }

    @Test
    fun `viernes 23-00 inicia el fin de semana VALLE de 50 horas`() {
        val rate = engine.currentRate(at("2026-01-09T23:00:00Z"))
        assertEquals(RateStatus.OFF_PEAK, rate.status)
        assertEquals(50.hours, rate.remaining)
        assertEquals(at("2026-01-12T01:00:00Z"), rate.window.end)
    }

    @Test
    fun `isPeak distingue viernes PEAK de sabado VALLE`() {
        assertTrue(engine.isPeak(at("2026-01-09T07:00:00Z")))
        assertFalse(engine.isPeak(at("2026-01-10T07:00:00Z")))
    }

    @Test
    fun `nextTransition devuelve el fin de la ventana actual`() {
        assertEquals(at("2026-01-05T04:00:00Z"), engine.nextTransition(at("2026-01-05T02:13:00Z")))
    }

    @Test
    fun `el progreso crece dentro de la ventana`() {
        val start = engine.currentRate(at("2026-01-05T06:00:00Z"))
        val middle = engine.currentRate(at("2026-01-05T08:00:00Z"))
        assertEquals(0f, start.progress, 0.0001f)
        assertEquals(0.5f, middle.progress, 0.0001f)
    }

    @Test
    fun `la ventana nunca se trunca y siempre existe una siguiente`() {
        // Recorre 10 días en pasos de 30 min: la ventana actual siempre debe
        // contener a `now`, terminar en el futuro y tener una ventana siguiente
        // con estado distinto.
        var now = at("2026-01-05T00:00:00Z")
        val end = at("2026-01-15T00:00:00Z")
        while (now < end) {
            val rate = engine.currentRate(now)
            assertTrue("window.start <= now en $now", rate.window.start <= now)
            assertTrue("window.end > now en $now", rate.window.end > now)
            assertTrue("window contiene a now en $now", rate.window.contains(now))
            val next = rate.next
            assertTrue("debe existir siguiente en $now", next != null)
            assertTrue("siguiente cambia de estado en $now", next!!.status != rate.status)
            assertEquals("ventanas contiguas en $now", rate.window.end, next.start)
            now = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + 30 * 60 * 1000L)
        }
    }

    @Test
    fun `isPeak solo es verdadero en los dos bloques de dia laborable`() {
        // Semana completa: solo lunes a viernes entre 01:00 y 09:59 UTC.
        var now = at("2026-01-05T00:00:00Z")
        val end = at("2026-01-12T00:00:00Z")
        while (now < end) {
            val local = now.toLocalDateTime(TimeZone.UTC)
            val isWeekday = local.dayOfWeek != DayOfWeek.SATURDAY &&
                local.dayOfWeek != DayOfWeek.SUNDAY
            val expected = isWeekday && (local.hour in 1..3 || local.hour in 6..9)
            assertEquals("estado en $now", expected, engine.isPeak(now))
            now = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + 60 * 60 * 1000L)
        }
    }
}
