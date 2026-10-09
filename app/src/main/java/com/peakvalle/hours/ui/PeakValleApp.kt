package com.peakvalle.hours.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.peakvalle.hours.domain.ScheduleEngine
import com.peakvalle.hours.ui.dashboard.DashboardRoute
import com.peakvalle.hours.ui.dashboard.DashboardScreen
import com.peakvalle.hours.ui.dashboard.DashboardUiState
import com.peakvalle.hours.ui.settings.SettingsRoute
import com.peakvalle.hours.ui.settings.SettingsScreen
import com.peakvalle.hours.ui.theme.PeakValleTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.hours

@Composable
fun PeakValleApp() {
    var showSettings by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showSettings) {
            SettingsRoute(onBack = { showSettings = false })
        } else {
            DashboardRoute(onOpenSettings = { showSettings = true })
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E1A)
@Composable
private fun DashboardPreview() {
    PeakValleTheme {
        val now = Clock.System.now()
        val engine = ScheduleEngine()
        DashboardScreen(
            state = DashboardUiState(
                now = now,
                rate = engine.currentRate(now),
                agenda = engine.windowsBetween(now, now + 24.hours)
            ),
            zone = TimeZone.currentSystemDefault()
        )
    }
}