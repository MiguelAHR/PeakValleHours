package com.peakvalle.hours.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peakvalle.hours.domain.ScheduleEngine
import com.peakvalle.hours.domain.model.CurrentRate
import com.peakvalle.hours.domain.model.RateWindow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

private const val AGENDA_WINDOW_HOURS = 24L

/** Estado completo de la pantalla principal. */
data class DashboardUiState(
    val now: Instant,
    val rate: CurrentRate,
    val agenda: List<RateWindow>
)

class DashboardViewModel(
    private val engine: ScheduleEngine = ScheduleEngine()
) : ViewModel() {

    /** Emite el instante actual una vez por segundo mientras haya observadores. */
    private val ticker = flow {
        while (true) {
            emit(Clock.System.now())
            delay(1_000)
        }
    }

    val state: StateFlow<DashboardUiState> = ticker
        .map(::buildState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(Clock.System.now())
        )

    private fun buildState(now: Instant): DashboardUiState = DashboardUiState(
        now = now,
        rate = engine.currentRate(now),
        agenda = engine.windowsBetween(now, now + AGENDA_WINDOW_HOURS.hours)
    )
}