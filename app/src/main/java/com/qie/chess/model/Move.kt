package com.qie.chess.model

enum class MoveFlag {
    NORMAL,
    DOUBLE_PAWN_PUSH,
    EN_PASSANT,
    CASTLE_KINGSIDE,
    CASTLE_QUEENSIDE,
    PROMOTION
}

/**
 * A single chess move. [promotionType] is set only when [flag] == PROMOTION.
 * [capturedPiece] is filled in by the engine when the move is generated/played,
 * used for undo and for captured-piece display.
 */
data class Move(
    val from: Square,
    val to: Square,
    val piece: Piece,
    val flag: MoveFlag = MoveFlag.NORMAL,
    val promotionType: PieceType? = null,
    val capturedPiece: Piece? = null
) {
    /** Standard algebraic-ish long form used internally, e.g. e2e4, e7e8q. */
    fun toLongAlgebraic(): String {
        val promo = if (flag == MoveFlag.PROMOTION && promotionType != null) {
            promotionType.fenChar.lowercaseChar().toString()
        } else ""
        return "$from$to$promo"
    }
}
