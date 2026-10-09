package com.peakvalle.hours.ui.dashboard

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.peakvalle.hours.R
import com.peakvalle.hours.data.alarm.TransitionScheduler
import com.peakvalle.hours.data.notification.Notifications
import com.peakvalle.hours.ui.components.NeonCard
import com.peakvalle.hours.ui.theme.OnSurfaceLight
import com.peakvalle.hours.ui.theme.OnSurfaceVariantLight

/**
 * Avisa al usuario si falta el permiso de notificaciones o el de alarma exacta,
 * que son los dos motivos por los que podría no recibir los avisos.
 */
@Composable
fun NotificationPermissionCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var notificationsGranted by remember { mutableStateOf(Notifications.areEnabled(context)) }
    var exactAlarmsGranted by remember { mutableStateOf(TransitionScheduler.canScheduleExact(context)) }

    // Se reevalúa al volver a la app: el usuario puede haberlo activado en ajustes.
    LifecycleResumeEffect(Unit) {
        notificationsGranted = Notifications.areEnabled(context)
        exactAlarmsGranted = TransitionScheduler.canScheduleExact(context)
        onPauseOrDispose { }
    }

    val needsNotificationPermission =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsGranted

    if (!needsNotificationPermission && exactAlarmsGranted) return

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
    }

    val accent = if (needsNotificationPermission) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    NeonCard(accent = accent, modifier = modifier) {
        Text(
            text = stringResource(R.string.perm_notifications_title),
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceLight
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(
                if (needsNotificationPermission) {
                    R.string.perm_notifications_body
                } else {
                    R.string.perm_exact_alarm_body
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantLight
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (needsNotificationPermission) {
                        requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(
                    text = stringResource(
                        if (needsNotificationPermission) {
                            R.string.perm_notifications_action
                        } else {
                            R.string.perm_exact_alarm_action
                        }
                    )
                )
            }
        }
    }
}