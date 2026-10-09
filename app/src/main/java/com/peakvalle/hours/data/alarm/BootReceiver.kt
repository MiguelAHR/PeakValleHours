package com.peakvalle.hours.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.peakvalle.hours.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Las alarmas se pierden al reiniciar o al cambiar la hora o la zona horaria,
 * así que se reagendan en esos eventos.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val pendingResult = goAsync()
                val appContext = context.applicationContext
                CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                    try {
                        val settings = SettingsRepository(appContext).settings.first()
                        TransitionScheduler.scheduleNext(appContext, settings)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}