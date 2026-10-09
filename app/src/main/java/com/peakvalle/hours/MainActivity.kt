package com.peakvalle.hours

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.peakvalle.hours.data.alarm.TransitionScheduler
import com.peakvalle.hours.data.notification.NotificationChannels
import com.peakvalle.hours.ui.PeakValleApp
import com.peakvalle.hours.ui.theme.PeakValleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationChannels.create(this)
        TransitionScheduler.scheduleNext(this)

        setContent {
            PeakValleTheme {
                PeakValleApp()
            }
        }
    }
}
