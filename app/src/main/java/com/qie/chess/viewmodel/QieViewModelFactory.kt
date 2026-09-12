package com.qie.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.qie.chess.QieChessApp

class QieViewModelFactory(private val app: QieChessApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(
                app.historyRepository, app.soundManager, app.haptics
            ) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
                app.settingsRepository
            ) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(
                app.historyRepository
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
