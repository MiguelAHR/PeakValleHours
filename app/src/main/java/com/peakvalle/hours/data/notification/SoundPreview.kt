package com.peakvalle.hours.data.notification

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.peakvalle.hours.domain.model.SoundType

/**
 * Reproduce una muestra del sonido configurado para los avisos.
 *
 * Los canales de notificación de Android son inmutables, así que no se puede
 * "probar" el canal directamente: se reproduce el mismo archivo o URI que
 * usaría la notificación.
 */
class SoundPreview(private val context: Context) {

    private var player: MediaPlayer? = null

    fun play(soundType: SoundType, soundUri: String) {
        release()

        val source: Uri? = when (soundType) {
            SoundType.DEFAULT ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            SoundType.CUSTOM ->
                soundUri.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

            SoundType.SILENT -> null
        } ?: return

        runCatching {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, source!!)
            mediaPlayer.prepare()
            mediaPlayer.setOnCompletionListener { release() }
            mediaPlayer.start()
            player = mediaPlayer
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}