package com.qie.chess.model

/**
 * Full, immutable snapshot of a chess position + enough history to support
 * en passant, castling, the fifty-move rule and threefold repetition.
 *
 * A new GameState is produced for every move (the [Board] itself is copied),
 * which makes undo, history scrubbing and the bot's search trivially safe.
 */
data class GameState(
    val board: Board,
    val sideToMove: PieceColor = PieceColor.WHITE,
    val castlingRights: CastlingRights = CastlingRights(),
    val enPassantTarget: Square? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1,
    val moveHistory: List<Move> = emptyList(),
    /** Position keys (board+side+castling+ep) for threefold-repetition detection. */
    val positionHistory: List<String> = emptyList(),
    val result: GameResult = GameResult.IN_PROGRESS
) {
    fun positionKey(): String {
        val sb = StringBuilder()
        for (r in 7 downTo 0) {
            for (f in 0..7) {
                val p = board.pieceAt(Square(f, r))
                sb.append(p?.fenChar() ?: '.')
            }
        }
        sb.append(sideToMove.name.first())
        sb.append(castlingRights.whiteKingSide).append(castlingRights.whiteQueenSide)
        sb.append(castlingRights.blackKingSide).append(castlingRights.blackQueenSide)
        sb.append(enPassantTarget?.toString() ?: "-")
        return sb.toString()
    }

    fun repetitionCount(): Int = positionHistory.count { it == positionKey() }

    companion object {
        fun newGame(): GameState {
            val base = GameState(board = Board.startingPosition())
            return base.copy(positionHistory = listOf(base.positionKey()))
        }
    }
}
