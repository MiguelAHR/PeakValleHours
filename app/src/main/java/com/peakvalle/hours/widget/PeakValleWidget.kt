package com.peakvalle.hours.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.peakvalle.hours.R
import com.peakvalle.hours.domain.ScheduleEngine
import com.peakvalle.hours.ui.theme.DeepSeekBlue
import com.peakvalle.hours.ui.theme.NeonPeak
import com.peakvalle.hours.ui.theme.NeonValle
import com.peakvalle.hours.ui.theme.OnSurfaceLight
import com.peakvalle.hours.ui.theme.OnSurfaceVariantLight
import com.peakvalle.hours.ui.util.toCountdown
import kotlinx.datetime.Clock

private val WidgetBackground = Color(0xFF0D1324)

/**
 * Widget de pantalla de inicio con el estado actual y la cuenta regresiva
 * hasta el próximo cambio de tarifa.
 *
 * Glance 1.1.1 todavía no ofrece un modificador `clickable`, así que el widget
 * es informativo. Se refresca en cada transición y cada 30 minutos.
 */
class PeakValleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rate = ScheduleEngine().currentRate(Clock.System.now())
        val accent = if (rate.isPeak) NeonPeak else NeonValle

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(WidgetBackground))
                    .padding(16.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                Text(
                    text = context.getString(R.string.brand_name),
                    style = TextStyle(
                        color = ColorProvider(DeepSeekBlue.toArgb()),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )

                Spacer(GlanceModifier.height(8.dp))

                Text(
                    text = context.getString(
                        if (rate.isPeak) R.string.status_peak else R.string.status_valle
                    ),
                    style = TextStyle(
                        color = ColorProvider(accent.toArgb()),
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                )

                Spacer(GlanceModifier.height(6.dp))

                Text(
                    text = rate.remaining.toCountdown(),
                    style = TextStyle(
                        color = ColorProvider(OnSurfaceLight.toArgb()),
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                )

                Spacer(GlanceModifier.height(4.dp))

                Text(
                    text = context.getString(R.string.countdown_label),
                    style = TextStyle(
                        color = ColorProvider(OnSurfaceVariantLight.toArgb()),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    companion object {
        /** Fuerza la actualización de todos los widgets colocados. */
        suspend fun refresh(context: Context) {
            PeakValleWidget().updateAll(context)
        }
    }
}

/** Receptor que registra el widget en el lanzador. */
class PeakValleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PeakValleWidget()
}