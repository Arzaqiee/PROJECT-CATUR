package com.qie.chess.engine

import com.qie.chess.model.*

object Fen {

    fun parse(fen: String): GameState {
        val parts = fen.trim().split(" ")
        val board = Board()
        val rows = parts[0].split("/")
        for (rIdx in rows.indices) {
            val rank = 7 - rIdx
            var file = 0
            for (c in rows[rIdx]) {
                if (c.isDigit()) {
                    file += c.digitToInt()
                } else {
                    Piece.fromFenChar(c)?.let { board.setPiece(Square(file, rank), it) }
                    file++
                }
            }
        }

        val sideToMove = if (parts.getOrNull(1) == "b") PieceColor.BLACK else PieceColor.WHITE

        val castlingStr = parts.getOrNull(2) ?: "-"
        val castling = CastlingRights(
            whiteKingSide = castlingStr.contains('K'),
            whiteQueenSide = castlingStr.contains('Q'),
            blackKingSide = castlingStr.contains('k'),
            blackQueenSide = castlingStr.contains('q')
        )

        val epStr = parts.getOrNull(3) ?: "-"
        val enPassant = if (epStr != "-") Square.fromAlgebraic(epStr) else null

        val halfmove = parts.getOrNull(4)?.toIntOrNull() ?: 0
        val fullmove = parts.getOrNull(5)?.toIntOrNull() ?: 1

        val state = GameState(
            board = board,
            sideToMove = sideToMove,
            castlingRights = castling,
            enPassantTarget = enPassant,
            halfmoveClock = halfmove,
            fullmoveNumber = fullmove
        )
        return state.copy(positionHistory = listOf(state.positionKey()))
    }

    fun export(state: GameState): String {
        val sb = StringBuilder()
        for (rank in 7 downTo 0) {
            var empty = 0
            for (file in 0..7) {
                val piece = state.board.pieceAt(Square(file, rank))
                if (piece == null) {
                    empty++
                } else {
                    if (empty > 0) {
                        sb.append(empty)
                        empty = 0
                    }
                    sb.append(piece.fenChar())
                }
            }
            if (empty > 0) sb.append(empty)
            if (rank != 0) sb.append('/')
        }
        sb.append(if (state.sideToMove == PieceColor.WHITE) " w " else " b ")

        val c = state.castlingRights
        val castleStr = buildString {
            if (c.whiteKingSide) append('K')
            if (c.whiteQueenSide) append('Q')
            if (c.blackKingSide) append('k')
            if (c.blackQueenSide) append('q')
        }
        sb.append(castleStr.ifEmpty { "-" })
        sb.append(' ')
        sb.append(state.enPassantTarget?.toString() ?: "-")
        sb.append(' ').append(state.halfmoveClock)
        sb.append(' ').append(state.fullmoveNumber)
        return sb.toString()
    }
}
