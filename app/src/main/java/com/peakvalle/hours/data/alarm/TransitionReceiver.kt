package com.peakvalle.hours.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.peakvalle.hours.data.notification.Notifications
import com.peakvalle.hours.domain.ScheduleEngine
import kotlinx.datetime.Clock

/**
 * Recibe la alarma exacta del cambio de tarifa: avisa al usuario y reagenda
 * la siguiente.
 */
class TransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val now = Clock.System.now()
        val rate = ScheduleEngine().currentRate(now)

        if (Notifications.areEnabled(context)) {
            Notifications.showTransition(context, rate)
        }

        // Siempre debe quedar una alarma pendiente para el próximo cambio.
        TransitionScheduler.scheduleNext(context, now)
    }

    companion object {
        const val ACTION_TRANSITION = "com.peakvalle.hours.action.TRANSITION"
    }
}