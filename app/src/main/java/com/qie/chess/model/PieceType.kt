package com.qie.chess.model

enum class PieceType(val fenChar: Char, val value: Int) {
    PAWN('p', 100),
    KNIGHT('n', 320),
    BISHOP('b', 330),
    ROOK('r', 500),
    QUEEN('q', 900),
    KING('k', 20000)
}
