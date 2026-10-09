package com.peakvalle.hours.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.peakvalle.hours.data.notification.Notifications
import com.peakvalle.hours.data.settings.SettingsRepository
import com.peakvalle.hours.domain.ScheduleEngine
import com.peakvalle.hours.widget.PeakValleWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Recibe las alarmas de cambio de tarifa y aviso anticipado: publica el aviso
 * correspondiente y reagenda siempre la siguiente transición.
 */
class TransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val isLead = intent?.action == ACTION_LEAD

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(appContext).settings.first()
                val now = Clock.System.now()
                val rate = ScheduleEngine().currentRate(now)

                if (settings.notificationsEnabled && Notifications.areEnabled(appContext)) {
                    if (isLead) {
                        Notifications.showUpcoming(appContext, rate)
                    } else {
                        Notifications.showTransition(
                            context = appContext,
                            rate = rate,
                            soundType = settings.soundType,
                            soundUri = settings.soundUri
                        )
                    }
                }

                // Siempre debe quedar una alarma pendiente para el próximo cambio.
                TransitionScheduler.scheduleNext(appContext, settings, now)

                // El widget muestra la misma cuenta regresiva: hay que refrescarlo.
                PeakValleWidget.refresh(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TRANSITION = "com.peakvalle.hours.action.TRANSITION"
        const val ACTION_LEAD = "com.peakvalle.hours.action.LEAD"
    }
}