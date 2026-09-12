package com.qie.chess

import android.app.Application
import com.qie.chess.audio.SoundManager
import com.qie.chess.data.HistoryRepository
import com.qie.chess.data.SettingsRepository
import com.qie.chess.util.Haptics

class QieChessApp : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var soundManager: SoundManager
        private set
    lateinit var haptics: Haptics
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        historyRepository = HistoryRepository(this)
        soundManager = SoundManager(this)
        haptics = Haptics(this)
    }
}
