package com.peakvalle.hours.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.peakvalle.hours.domain.ScheduleEngine
import com.peakvalle.hours.domain.model.AppSettings
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Programa la alarma exacta del próximo cambio de tarifa y la reagenda sola
 * cada vez que se dispara, de modo que siempre haya exactamente una pendiente.
 */
object TransitionScheduler {

    private const val REQUEST_TRANSITION = 1001
    private const val REQUEST_LEAD = 1002

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

    /**
     * Agenda el aviso de cambio y, si está configurado, el aviso anticipado.
     */
    fun scheduleNext(
        context: Context,
        settings: AppSettings,
        now: Instant = Clock.System.now()
    ): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return false
        if (!settings.notificationsEnabled) {
            cancel(context)
            return false
        }

        val exact = canScheduleExact(context)
        val transitionAt = engine.nextTransition(now)

        setAlarm(
            manager = manager,
            context = context,
            triggerAt = transitionAt,
            requestCode = REQUEST_TRANSITION,
            action = TransitionReceiver.ACTION_TRANSITION,
            exact = exact
        )

        if (settings.leadMinutes > 0) {
            val leadAt = transitionAt - settings.leadMinutes.minutes
            if (leadAt > now) {
                setAlarm(
                    manager = manager,
                    context = context,
                    triggerAt = leadAt,
                    requestCode = REQUEST_LEAD,
                    action = TransitionReceiver.ACTION_LEAD,
                    exact = exact
                )
            }
        } else {
            cancelLead(context)
        }

        return true
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        manager.cancel(pendingIntent(context, REQUEST_TRANSITION, TransitionReceiver.ACTION_TRANSITION))
        cancelLead(context)
    }

    private fun cancelLead(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        manager.cancel(pendingIntent(context, REQUEST_LEAD, TransitionReceiver.ACTION_LEAD))
    }

    private fun setAlarm(
        manager: AlarmManager,
        context: Context,
        triggerAt: Instant,
        requestCode: Int,
        action: String,
        exact: Boolean
    ) {
        val operation = pendingIntent(context, requestCode, action)
        val triggerAtMs = triggerAt.toEpochMilliseconds()

        try {
            if (exact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, operation)
            } else {
                // Sin permiso de alarma exacta: aviso aproximado pero igualmente útil.
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, operation)
            }
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, operation)
        }
    }

    private fun pendingIntent(context: Context, requestCode: Int, action: String): PendingIntent {
        val intent = Intent(context, TransitionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}