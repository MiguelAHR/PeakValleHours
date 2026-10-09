package com.peakvalle.hours.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.peakvalle.hours.domain.ScheduleEngine
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Programa la alarma exacta del próximo cambio de tarifa y la reagenda sola
 * cada vez que se dispara, de modo que siempre haya exactamente una pendiente.
 */
object TransitionScheduler {

    private const val REQUEST_CODE = 1001

    private val engine = ScheduleEngine()

    /** Indica si el sistema permite alarmas exactas. */
    fun canScheduleExact(context: Context): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /** Agenda la alarma para el próximo cambio contado desde [now]. */
    fun scheduleNext(context: Context, now: Instant = Clock.System.now()): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return false
        val target = engine.nextTransition(now)
        val operation = pendingIntent(context)

        return try {
            if (canScheduleExact(context)) {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.toEpochMilliseconds(),
                    operation
                )
            } else {
                // Sin permiso de alarma exacta: aviso aproximado pero igualmente útil.
                manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.toEpochMilliseconds(),
                    operation
                )
            }
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TransitionReceiver::class.java)
            .setAction(TransitionReceiver.ACTION_TRANSITION)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}