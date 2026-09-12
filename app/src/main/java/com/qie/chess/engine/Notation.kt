package com.qie.chess.engine

import com.qie.chess.model.*

/**
 * Converts moves into standard algebraic notation (SAN), given the position
 * *before* the move was played, and builds full PGN move text.
 */
object Notation {

    fun toSan(stateBeforeMove: GameState, move: Move, stateAfterMove: GameState): String {
        if (move.flag == MoveFlag.CASTLE_KINGSIDE) return withCheckSuffix("O-O", stateAfterMove)
        if (move.flag == MoveFlag.CASTLE_QUEENSIDE) return withCheckSuffix("O-O-O", stateAfterMove)

        val sb = StringBuilder()
        val isCapture = move.capturedPiece != null

        if (move.piece.type == PieceType.PAWN) {
            if (isCapture) sb.append(('a' + move.from.file)).append('x')
            sb.append(move.to.toString())
            if (move.flag == MoveFlag.PROMOTION && move.promotionType != null) {
                sb.append('=').append(move.promotionType.fenChar.uppercaseChar())
            }
        } else {
            sb.append(pieceLetter(move.piece.type))
            sb.append(disambiguation(stateBeforeMove, move))
            if (isCapture) sb.append('x')
            sb.append(move.to.toString())
        }

        return withCheckSuffix(sb.toString(), stateAfterMove)
    }

    private fun withCheckSuffix(san: String, stateAfterMove: GameState): String {
        return when (stateAfterMove.result) {
            GameResult.WHITE_WINS_CHECKMATE, GameResult.BLACK_WINS_CHECKMATE -> "$san#"
            else -> if (ChessEngine.isInCheck(stateAfterMove, stateAfterMove.sideToMove)) "$san+" else san
        }
    }

    private fun pieceLetter(type: PieceType): Char = when (type) {
        PieceType.KNIGHT -> 'N'
        PieceType.BISHOP -> 'B'
        PieceType.ROOK -> 'R'
        PieceType.QUEEN -> 'Q'
        PieceType.KING -> 'K'
        PieceType.PAWN -> ' '
    }

    /** Adds file/rank/both when another same-type piece could reach the same square. */
    private fun disambiguation(state: GameState, move: Move): String {
        val others = ChessEngine.legalMoves(state).filter {
            it.to == move.to &&
                it.from != move.from &&
                it.piece.type == move.piece.type &&
                it.piece.color == move.piece.color
        }
        if (others.isEmpty()) return ""

        val sameFile = others.any { it.from.file == move.from.file }
        val sameRank = others.any { it.from.rank == move.from.rank }

        return when {
            !sameFile -> ('a' + move.from.file).toString()
            !sameRank -> ('1' + move.from.rank).toString()
            else -> move.from.toString()
        }
    }

    /** Builds a full PGN move-text string, e.g. "1. e4 e5 2. Nf3 Nc6". */
    fun buildPgnMoveText(sanMoves: List<String>): String {
        val sb = StringBuilder()
        for (i in sanMoves.indices) {
            if (i % 2 == 0) {
                sb.append(i / 2 + 1).append(". ")
            }
            sb.append(sanMoves[i]).append(' ')
        }
        return sb.toString().trim()
    }

    fun resultTag(result: GameResult): String = when (result) {
        GameResult.WHITE_WINS_CHECKMATE, GameResult.WHITE_WINS_RESIGN, GameResult.WHITE_WINS_TIMEOUT -> "1-0"
        GameResult.BLACK_WINS_CHECKMATE, GameResult.BLACK_WINS_RESIGN, GameResult.BLACK_WINS_TIMEOUT -> "0-1"
        GameResult.IN_PROGRESS -> "*"
        else -> "1/2-1/2"
    }
}
