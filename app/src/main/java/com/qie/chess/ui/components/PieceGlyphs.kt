package com.qie.chess.ui.components

import com.qie.chess.model.Piece
import com.qie.chess.model.PieceColor
import com.qie.chess.model.PieceType

/**
 * Standard Unicode chess symbols (U+2654-U+265F) - crisp at any size, no
 * bitmap assets needed, and keeps the board lightweight.
 */
object PieceGlyphs {
    fun glyph(piece: Piece): String = when (piece.color) {
        PieceColor.WHITE -> when (piece.type) {
            PieceType.KING -> "\u2654"
            PieceType.QUEEN -> "\u2655"
            PieceType.ROOK -> "\u2656"
            PieceType.BISHOP -> "\u2657"
            PieceType.KNIGHT -> "\u2658"
            PieceType.PAWN -> "\u2659"
        }
        PieceColor.BLACK -> when (piece.type) {
            PieceType.KING -> "\u265A"
            PieceType.QUEEN -> "\u265B"
            PieceType.ROOK -> "\u265C"
            PieceType.BISHOP -> "\u265D"
            PieceType.KNIGHT -> "\u265E"
            PieceType.PAWN -> "\u265F"
        }
    }
}
