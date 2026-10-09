package com.peakvalle.hours

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.peakvalle.hours.data.alarm.TransitionScheduler
import com.peakvalle.hours.data.notification.NotificationChannels
import com.peakvalle.hours.data.notification.Notifications
import com.peakvalle.hours.data.settings.SettingsRepository
import com.peakvalle.hours.domain.ScheduleEngine
import com.peakvalle.hours.ui.PeakValleApp
import com.peakvalle.hours.ui.theme.PeakValleTheme
import com.peakvalle.hours.widget.PeakValleWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationChannels.create(this)

        lifecycleScope.launch {
            val settings = SettingsRepository(applicationContext).settings.first()
            NotificationChannels.create(this@MainActivity, settings.soundType, settings.soundUri)
            TransitionScheduler.scheduleNext(this@MainActivity, settings)
            PeakValleWidget.refresh(this@MainActivity)

            val engine = ScheduleEngine()
            val rate = engine.currentRate(Clock.System.now())
            if (settings.ongoingEnabled && Notifications.areEnabled(this@MainActivity)) {
                Notifications.showOngoing(this@MainActivity, rate)
            } else {
                Notifications.cancelOngoing(this@MainActivity)
            }
        }

        setContent {
            PeakValleTheme {
                PeakValleApp()
            }
        }
    }
}