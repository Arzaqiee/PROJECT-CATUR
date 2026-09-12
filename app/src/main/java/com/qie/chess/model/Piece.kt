package com.qie.chess.model

data class Piece(
    val type: PieceType,
    val color: PieceColor
) {
    /** FEN-style character: uppercase for white, lowercase for black. */
    fun fenChar(): Char =
        if (color == PieceColor.WHITE) type.fenChar.uppercaseChar() else type.fenChar

    companion object {
        fun fromFenChar(c: Char): Piece? {
            val type = when (c.lowercaseChar()) {
                'p' -> PieceType.PAWN
                'n' -> PieceType.KNIGHT
                'b' -> PieceType.BISHOP
                'r' -> PieceType.ROOK
                'q' -> PieceType.QUEEN
                'k' -> PieceType.KING
                else -> return null
            }
            val color = if (c.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
            return Piece(type, color)
        }
    }
}
