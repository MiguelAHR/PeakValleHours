package com.peakvalle.hours.ui.dashboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peakvalle.hours.R
import com.peakvalle.hours.domain.model.CurrentRate
import com.peakvalle.hours.domain.model.RateStatus
import com.peakvalle.hours.domain.model.RateWindow
import com.peakvalle.hours.ui.components.NeonCard
import com.peakvalle.hours.ui.theme.BgBottom
import com.peakvalle.hours.ui.theme.BgTop
import com.peakvalle.hours.ui.theme.NeonPeak
import com.peakvalle.hours.ui.theme.NeonValle
import com.peakvalle.hours.ui.theme.OnSurfaceLight
import com.peakvalle.hours.ui.theme.OnSurfaceVariantLight
import com.peakvalle.hours.ui.util.formatLocalRange
import com.peakvalle.hours.ui.util.formatLocalTime
import com.peakvalle.hours.ui.util.toCoarse
import com.peakvalle.hours.ui.util.toCountdown
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

/** Color de acento según el estado de la tarifa. */
fun RateStatus.accent(): Color = when (this) {
    RateStatus.PEAK -> NeonPeak
    RateStatus.OFF_PEAK -> NeonValle
}

@Composable
fun DashboardRoute(
    zone: TimeZone = TimeZone.currentSystemDefault(),
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DashboardScreen(state = state, zone = zone)
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    zone: TimeZone = TimeZone.currentSystemDefault(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(state.now, zone)
            NotificationPermissionCard()
            StatusCard(state.rate, zone)
            NextChangeCard(state.rate, zone)
            AgendaCard(state.agenda, zone)
        }
    }
}

@Composable
private fun Header(now: Instant, zone: TimeZone) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.brand_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = zone.id,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariantLight
            )
        }
        Text(
            text = now.formatLocalTime(zone, withSeconds = true),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Monospace,
            color = OnSurfaceLight
        )
    }
}

@Composable
private fun StatusCard(rate: CurrentRate, zone: TimeZone) {
    val accent = rate.status.accent()

    NeonCard(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulsingDot(accent)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.status_now),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariantLight
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(
                if (rate.isPeak) R.string.status_peak else R.string.status_valle
            ),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = accent
        )
        Text(
            text = stringResource(
                if (rate.isPeak) R.string.price_peak else R.string.price_valle
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantLight
        )

        Spacer(Modifier.height(22.dp))

        Text(
            text = rate.remaining.toCountdown(),
            style = MaterialTheme.typography.displayMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceLight
        )
        Text(
            text = stringResource(R.string.countdown_label),
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariantLight
        )

        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { rate.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accent
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${formatLocalRange(rate.window.start, rate.window.end, zone)} · ${rate.total.toCoarse()}",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariantLight
        )
    }
}

@Composable
private fun NextChangeCard(rate: CurrentRate, zone: TimeZone) {
    val next = rate.next ?: return
    val accent = next.status.accent()

    NeonCard(accent = accent) {
        Text(
            text = stringResource(R.string.next_change),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariantLight
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    if (rate.isPeak) R.string.status_valle else R.string.status_peak
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatLocalRange(next.start, next.end, zone),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = OnSurfaceLight
            )
        }
    }
}

@Composable
private fun AgendaCard(windows: List<RateWindow>, zone: TimeZone) {
    if (windows.isEmpty()) return

    NeonCard(accent = MaterialTheme.colorScheme.primary) {
        Text(
            text = stringResource(R.string.agenda_24h),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariantLight
        )
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            windows.forEach { window ->
                val share = window.duration.inWholeMilliseconds.toFloat().coerceAtLeast(1f)
                Box(
                    modifier = Modifier
                        .weight(share)
                        .fillMaxHeight()
                        .background(window.status.accent().copy(alpha = 0.85f))
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = windows.first().start.formatLocalTime(zone),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = OnSurfaceVariantLight
            )
            Text(
                text = windows.last().end.formatLocalTime(zone),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = OnSurfaceVariantLight
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Legend(RateStatus.PEAK, R.string.legend_peak)
            Legend(RateStatus.OFF_PEAK, R.string.legend_valle)
        }
    }
}

@Composable
private fun Legend(status: RateStatus, labelRes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(status.accent())
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariantLight
        )
    }
}

@Composable
private fun PulsingDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}