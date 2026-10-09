package com.peakvalle.hours.data.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.peakvalle.hours.MainActivity
import com.peakvalle.hours.R
import com.peakvalle.hours.domain.model.CurrentRate
import com.peakvalle.hours.domain.model.SoundType
import com.peakvalle.hours.ui.util.formatLocalTime
import kotlinx.datetime.TimeZone

object Notifications {

    /** Id de la notificación de cambio de tarifa. */
    const val TRANSITION_ID = 2001

    /** Id del aviso anticipado. */
    const val LEAD_ID = 2002

    fun areEnabled(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            areSystemNotificationsEnabled(context)
        }

    private fun areSystemNotificationsEnabled(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java)
            ?.areNotificationsEnabled() == true

    /** Publica el aviso de cambio de tarifa que acaba de ocurrir. */
    fun showTransition(
        context: Context,
        rate: CurrentRate,
        soundType: SoundType = SoundType.DEFAULT,
        soundUri: String = ""
    ) {
        val channelId = NotificationChannels.channelIdFor(soundType, soundUri)
        NotificationChannels.create(context, soundType, soundUri)

        val zone = TimeZone.currentSystemDefault()
        val nextStart = rate.next?.start

        val titleRes = if (rate.isPeak) R.string.notif_title_peak else R.string.notif_title_valle
        val bodyRes = if (rate.isPeak) R.string.notif_body_peak else R.string.notif_body_valle

        val body = if (nextStart != null) {
            context.getString(bodyRes, nextStart.formatLocalTime(zone))
        } else {
            context.getString(bodyRes, "—")
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(titleRes))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()

        notify(context, TRANSITION_ID, notification)
    }

    /** Publica el aviso anticipado antes del cambio de tarifa. */
    fun showUpcoming(context: Context, rate: CurrentRate) {
        NotificationChannels.create(context)

        val zone = TimeZone.currentSystemDefault()
        val target = rate.window.end
        val nextLabel = if (rate.isPeak) R.string.status_valle else R.string.status_peak

        val title = context.getString(R.string.notif_title_upcoming, context.getString(nextLabel))
        val body = context.getString(R.string.notif_body_upcoming, target.formatLocalTime(zone))

        val notification = NotificationCompat.Builder(context, NotificationChannels.STATUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()

        notify(context, LEAD_ID, notification)
    }

    private fun notify(context: Context, id: Int, notification: android.app.Notification) {
        context.getSystemService(NotificationManager::class.java)?.notify(id, notification)
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}