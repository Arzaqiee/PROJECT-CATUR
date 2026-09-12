package com.qie.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qie.chess.data.HistoryRepository
import com.qie.chess.model.GameHistoryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    val entries: StateFlow<List<GameHistoryEntry>> = repository.historyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun clearHistory() {
        viewModelScope.launch { repository.clear() }
    }
}
