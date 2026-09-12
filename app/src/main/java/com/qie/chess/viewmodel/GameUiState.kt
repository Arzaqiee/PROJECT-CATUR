package com.qie.chess.viewmodel

import com.qie.chess.bot.BotDifficulty
import com.qie.chess.model.*

data class GameUiState(
    val gameState: GameState = GameState.newGame(),
    val mode: GameMode = GameMode.VS_BOT,
    val playerColor: PieceColor = PieceColor.WHITE,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
    val selectedSquare: Square? = null,
    val legalMovesForSelected: List<Move> = emptyList(),
    val isBotThinking: Boolean = false,
    val pendingPromotion: PendingPromotion? = null,
    val timeControl: TimeControl = TimeControl.NONE,
    val whiteTimeMillis: Long = TimeControl.NONE.initialMillis,
    val blackTimeMillis: Long = TimeControl.NONE.initialMillis,
    val sanMoves: List<String> = emptyList(),
    val gameOverMessage: String? = null,
    val boardFlipped: Boolean = false
) {
    val lastMove: Move? get() = gameState.moveHistory.lastOrNull()
    val isGameOver: Boolean get() = gameState.result.isGameOver
    val capturedByWhite: List<PieceType> get() = capturedPieces(PieceColor.BLACK)
    val capturedByBlack: List<PieceType> get() = capturedPieces(PieceColor.WHITE)

    private fun capturedPieces(capturedColor: PieceColor): List<PieceType> =
        gameState.moveHistory.mapNotNull { it.capturedPiece }
            .filter { it.color == capturedColor }
            .map { it.type }
}

data class PendingPromotion(val from: Square, val to: Square, val color: PieceColor)
