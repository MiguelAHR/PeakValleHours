package com.peakvalle.hours.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import com.peakvalle.hours.R
import com.peakvalle.hours.domain.model.SoundType

object NotificationChannels {

    /** Prefijo de los canales de aviso; el sufijo codifica el sonido elegido. */
    private const val TRANSITIONS_PREFIX = "peakvalle.transitions"

    /** Estado actual, sin sonido y persistente. */
    const val STATUS = "peakvalle.status"

    /**
     * Los canales son inmutables en Android: cambiar el sonido implica crear un
     * canal nuevo con otro id, así que el id incluye el tipo de sonido.
     */
    fun channelIdFor(soundType: SoundType, soundUri: String): String {
        val key = when (soundType) {
            SoundType.DEFAULT -> "default"
            SoundType.SILENT -> "silent"
            SoundType.CUSTOM -> "custom-" + soundUri.hashCode().toUInt().toString(16)
        }
        return "$TRANSITIONS_PREFIX.$key"
    }

    fun create(context: Context) {
        create(context, SoundType.DEFAULT, "")
    }

    fun create(context: Context, soundType: SoundType, soundUri: String) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val status = NotificationChannel(
            STATUS,
            context.getString(R.string.channel_status_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_status_desc)
            setShowBadge(false)
        }

        val transitions = NotificationChannel(
            channelIdFor(soundType, soundUri),
            context.getString(R.string.channel_transitions_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_transitions_desc)
            enableVibration(soundType != SoundType.SILENT)
            setShowBadge(true)
            when (soundType) {
                SoundType.DEFAULT -> Unit
                SoundType.SILENT -> setSound(null, null)
                SoundType.CUSTOM -> setSound(
                    Uri.parse(soundUri),
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
        }

        // Elimina los canales de aviso de otros sonidos para no acumularlos.
        val keep = setOf(transitions.id, STATUS)
        manager.notificationChannels
            ?.filter { it.id.startsWith("$TRANSITIONS_PREFIX.") && it.id !in keep }
            ?.forEach { manager.deleteNotificationChannel(it.id) }

        manager.createNotificationChannels(listOf(transitions, status))
    }
}