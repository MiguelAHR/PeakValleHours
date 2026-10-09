package com.peakvalle.hours.domain.model

/**
 * Tarifa vigente de DeepSeek según la franja horaria (definida en UTC).
 */
enum class RateStatus {
    /** Franja PEAK: precio completo. */
    PEAK,

    /** Franja VALLE (off-peak): 50% de descuento. */
    OFF_PEAK
}
