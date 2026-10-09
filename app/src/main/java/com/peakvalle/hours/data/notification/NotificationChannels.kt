package com.peakvalle.hours.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.peakvalle.hours.R

object NotificationChannels {

    /** Avisos sonoros al cambiar de tarifa. */
    const val TRANSITIONS = "peakvalle.transitions"

    /** Estado actual, sin sonido y persistente. */
    const val STATUS = "peakvalle.status"

    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val transitions = NotificationChannel(
            TRANSITIONS,
            context.getString(R.string.channel_transitions_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_transitions_desc)
            enableVibration(true)
            setShowBadge(true)
        }

        val status = NotificationChannel(
            STATUS,
            context.getString(R.string.channel_status_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_status_desc)
            setShowBadge(false)
        }

        manager.createNotificationChannels(listOf(transitions, status))
    }
}