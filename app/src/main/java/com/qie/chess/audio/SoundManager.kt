package com.qie.chess.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

enum class SoundEvent { MOVE, CAPTURE, CHECK, CHECKMATE, CLICK, GAME_START, GAME_FINISH }

/**
 * Thin wrapper around [SoundPool].
 *
 * NOTE: the actual .mp3/.ogg assets are NOT bundled in this source drop (binary
 * audio can't be generated here). Drop short sound effects into
 * app/src/main/res/raw/ using these exact file names and everything will
 * "just work":
 *   move.mp3, capture.mp3, check.mp3, checkmate.mp3, click.mp3, start.mp3, finish.mp3
 * Free CC0 options: freesound.org, mixkit.co (search "chess" / "click").
 * Until then, calls are safely no-ops (resource lookups are guarded).
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private var enabled = true

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<SoundEvent, Int>()

    init {
        loadIfPresent(SoundEvent.MOVE, "move")
        loadIfPresent(SoundEvent.CAPTURE, "capture")
        loadIfPresent(SoundEvent.CHECK, "check")
        loadIfPresent(SoundEvent.CHECKMATE, "checkmate")
        loadIfPresent(SoundEvent.CLICK, "click")
        loadIfPresent(SoundEvent.GAME_START, "start")
        loadIfPresent(SoundEvent.GAME_FINISH, "finish")
    }

    private fun loadIfPresent(event: SoundEvent, resName: String) {
        val resId = appContext.resources.getIdentifier(resName, "raw", appContext.packageName)
        if (resId != 0) {
            soundIds[event] = soundPool.load(appContext, resId, 1)
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun play(event: SoundEvent) {
        if (!enabled) return
        soundIds[event]?.let { id -> soundPool.play(id, 1f, 1f, 1, 0, 1f) }
    }

    fun release() {
        soundPool.release()
    }
}
