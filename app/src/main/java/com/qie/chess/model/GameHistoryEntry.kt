package com.qie.chess.model

data class GameHistoryEntry(
    val id: String,
    val dateEpochMillis: Long,
    val mode: GameMode,
    val result: GameResult,
    val playerColor: PieceColor,
    val opponentLabel: String,
    val moveCount: Int,
    val pgn: String
)
