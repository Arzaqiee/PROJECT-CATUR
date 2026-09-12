package com.qie.chess.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticEvent { MOVE, CAPTURE, CHECK, GAME_END }

class Haptics(context: Context) {

    private val appContext = context.applicationContext
    private var enabled = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun trigger(event: HapticEvent) {
        if (!enabled) return
        val durationMs = when (event) {
            HapticEvent.MOVE -> 10L
            HapticEvent.CAPTURE -> 20L
            HapticEvent.CHECK -> 35L
            HapticEvent.GAME_END -> 60L
        }
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }
}
