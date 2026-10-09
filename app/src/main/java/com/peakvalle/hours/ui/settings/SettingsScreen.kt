package com.peakvalle.hours.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peakvalle.hours.R
import com.peakvalle.hours.data.notification.NotificationChannels
import com.peakvalle.hours.data.notification.SoundPreview
import com.peakvalle.hours.domain.model.AppSettings
import com.peakvalle.hours.domain.model.SoundType
import com.peakvalle.hours.ui.components.NeonCard
import com.peakvalle.hours.ui.theme.BgBottom
import com.peakvalle.hours.ui.theme.BgTop
import com.peakvalle.hours.ui.theme.OnSurfaceLight
import com.peakvalle.hours.ui.theme.OnSurfaceVariantLight

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onBack = onBack,
        onNotificationsChanged = viewModel::setNotificationsEnabled,
        onLeadMinutesChanged = viewModel::setLeadMinutes,
        onSoundChanged = viewModel::setSound,
        onOngoingChanged = viewModel::setOngoingEnabled
    )
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onLeadMinutesChanged: (Int) -> Unit,
    onSoundChanged: (SoundType, String) -> Unit,
    onOngoingChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val preview = remember { SoundPreview(context) }
    DisposableEffect(Unit) { onDispose { preview.release() } }

    val pickSound = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Persiste el permiso de lectura para poder reutilizar el audio.
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onSoundChanged(SoundType.CUSTOM, uri.toString())
            NotificationChannels.create(context, SoundType.CUSTOM, uri.toString())
        }
    }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.settings_back),
                        tint = OnSurfaceLight
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            NotificationsCard(
                settings = settings,
                onNotificationsChanged = onNotificationsChanged,
                onLeadMinutesChanged = onLeadMinutesChanged,
                onOngoingChanged = onOngoingChanged
            )

            SoundCard(
                settings = settings,
                onSoundChanged = { type, uri ->
                    onSoundChanged(type, uri)
                    NotificationChannels.create(context, type, uri)
                },
                onPickCustom = { pickSound.launch(arrayOf("audio/*")) },
                onPreview = { preview.play(settings.soundType, settings.soundUri) }
            )

            InfoCard()
        }
    }
}

@Composable
private fun NotificationsCard(
    settings: AppSettings,
    onNotificationsChanged: (Boolean) -> Unit,
    onLeadMinutesChanged: (Int) -> Unit,
    onOngoingChanged: (Boolean) -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary

    NeonCard(accent = accent) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_notifications),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight
                )
                Text(
                    text = stringResource(R.string.settings_notifications_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLight
                )
            }
            Switch(
                checked = settings.notificationsEnabled,
                onCheckedChange = onNotificationsChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accent,
                    checkedTrackColor = accent.copy(alpha = 0.35f)
                )
            )
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.settings_advance),
            style = MaterialTheme.typography.titleSmall,
            color = OnSurfaceLight
        )
        Text(
            text = stringResource(R.string.settings_advance_desc),
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantLight
        )
        Spacer(Modifier.height(10.dp))

        OptionRow(
            options = AppSettings.LEAD_OPTIONS,
            selected = settings.leadMinutes,
            accent = accent,
            label = { minutes ->
                if (minutes == 0) stringResource(R.string.settings_lead_off) else "$minutes min"
            },
            onSelect = onLeadMinutesChanged
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_ongoing),
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceLight
                )
                Text(
                    text = stringResource(R.string.settings_ongoing_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLight
                )
            }
            Switch(
                checked = settings.ongoingEnabled,
                onCheckedChange = onOngoingChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accent,
                    checkedTrackColor = accent.copy(alpha = 0.35f)
                )
            )
        }
    }
}

@Composable
private fun SoundCard(
    settings: AppSettings,
    onSoundChanged: (SoundType, String) -> Unit,
    onPickCustom: () -> Unit,
    onPreview: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.secondary

    NeonCard(accent = accent) {
        Text(
            text = stringResource(R.string.settings_sound),
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceLight
        )
        Text(
            text = stringResource(R.string.settings_sound_desc),
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantLight
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SoundChip(
                label = stringResource(R.string.settings_sound_default),
                selected = settings.soundType == SoundType.DEFAULT,
                accent = accent,
                onClick = { onSoundChanged(SoundType.DEFAULT, settings.soundUri) }
            )
            SoundChip(
                label = stringResource(R.string.settings_sound_silent),
                selected = settings.soundType == SoundType.SILENT,
                accent = accent,
                onClick = { onSoundChanged(SoundType.SILENT, settings.soundUri) }
            )
        }

        Spacer(Modifier.height(10.dp))

        SoundChip(
            label = if (settings.soundType == SoundType.CUSTOM) {
                stringResource(R.string.settings_sound_custom_selected)
            } else {
                stringResource(R.string.settings_sound_custom)
            },
            selected = settings.soundType == SoundType.CUSTOM,
            accent = accent,
            onClick = onPickCustom
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onPreview,
            enabled = settings.soundType != SoundType.SILENT
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = accent
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.settings_sound_preview),
                color = if (settings.soundType == SoundType.SILENT) {
                    OnSurfaceVariantLight
                } else {
                    accent
                }
            )
        }
    }
}

@Composable
private fun InfoCard() {
    NeonCard(accent = OnSurfaceVariantLight) {
        Text(
            text = stringResource(R.string.settings_schedule_title),
            style = MaterialTheme.typography.titleSmall,
            color = OnSurfaceLight
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_schedule_utc),
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantLight
        )
        Text(
            text = stringResource(R.string.settings_schedule_local),
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantLight
        )
    }
}

@Composable
private fun SoundChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.25f),
            selectedLabelColor = accent
        )
    )
}

/** Fila horizontal de opciones excluyentes, resaltando la activa. */
@Composable
private fun OptionRow(
    options: List<Int>,
    selected: Int,
    accent: Color,
    label: @Composable (Int) -> String,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) accent.copy(alpha = 0.25f) else Color.Transparent
                    )
                    .selectable(selected = isSelected, onClick = { onSelect(option) })
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) accent else OnSurfaceVariantLight
                )
            }
        }
    }
}