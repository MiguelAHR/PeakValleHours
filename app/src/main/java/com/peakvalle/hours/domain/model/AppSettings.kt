package com.peakvalle.hours.domain.model

/** Tipo de sonido para el aviso de cambio de tarifa. */
enum class SoundType {
    /** Sonido por defecto del sistema. */
    DEFAULT,

    /** Sin sonido. */
    SILENT,

    /** Archivo de audio elegido por el usuario. */
    CUSTOM
}

/**
 * Ajustes de la aplicación.
 *
 * @property leadMinutes minutos de antelación del aviso previo (0 = solo al cambiar).
 */
data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val leadMinutes: Int = 0,
    val soundType: SoundType = SoundType.DEFAULT,
    val soundUri: String = "",
    val ongoingEnabled: Boolean = false
) {
    companion object {
        /** Opciones de antelación ofrecidas en ajustes, en minutos. */
        val LEAD_OPTIONS = listOf(0, 5, 10, 15, 30)
    }
}