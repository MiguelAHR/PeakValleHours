package com.peakvalle.hours.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PeakValleColorScheme = darkColorScheme(
    primary = DeepSeekBlue,
    onPrimary = Color.White,
    primaryContainer = DeepSeekBlueDark,
    onPrimaryContainer = Color.White,

    secondary = NeonValle,
    onSecondary = BgTop,
    tertiary = NeonPeak,
    onTertiary = Color.White,

    background = BgTop,
    onBackground = OnSurfaceLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OnSurfaceVariantLight
)

@Composable
fun PeakValleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PeakValleColorScheme,
        typography = PeakValleTypography,
        content = content
    )
}
