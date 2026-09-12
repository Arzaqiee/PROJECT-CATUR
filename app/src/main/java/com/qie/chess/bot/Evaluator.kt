package com.qie.chess.bot

import com.qie.chess.model.GameState
import com.qie.chess.model.PieceColor

/**
 * Static position evaluation in centipawns, from White's perspective:
 * positive = good for White, negative = good for Black.
 */
object Evaluator {

    fun evaluate(state: GameState): Int {
        var score = 0
        for ((square, piece) in state.board.allPieces()) {
            val isWhite = piece.color == PieceColor.WHITE
            val material = piece.type.value
            val positional = PieceSquareTables.valueFor(piece.type, square.file, square.rank, isWhite)
            val total = material + positional
            score += if (isWhite) total else -total
        }
        return score
    }
}
