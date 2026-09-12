package com.qie.chess.model

/**
 * Mutable 8x8 board. Indexed as squares[rank][file], rank 0 = "1", file 0 = "a".
 */
class Board private constructor(
    private val squares: Array<Array<Piece?>>
) {
    constructor() : this(Array(8) { arrayOfNulls<Piece>(8) })

    fun pieceAt(square: Square): Piece? = squares[square.rank][square.file]

    fun setPiece(square: Square, piece: Piece?) {
        squares[square.rank][square.file] = piece
    }

    fun copy(): Board {
        val newSquares = Array(8) { r -> Array(8) { f -> squares[r][f] } }
        return Board(newSquares)
    }

    fun allPieces(): List<Pair<Square, Piece>> {
        val result = mutableListOf<Pair<Square, Piece>>()
        for (r in 0..7) for (f in 0..7) {
            squares[r][f]?.let { result.add(Square(f, r) to it) }
        }
        return result
    }

    fun findKing(color: PieceColor): Square? {
        for (r in 0..7) for (f in 0..7) {
            val p = squares[r][f]
            if (p != null && p.color == color && p.type == PieceType.KING) {
                return Square(f, r)
            }
        }
        return null
    }

    companion object {
        /** Standard chess starting position. */
        fun startingPosition(): Board {
            val board = Board()
            val backRank = listOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            )
            for (f in 0..7) {
                board.setPiece(Square(f, 0), Piece(backRank[f], PieceColor.WHITE))
                board.setPiece(Square(f, 1), Piece(PieceType.PAWN, PieceColor.WHITE))
                board.setPiece(Square(f, 6), Piece(PieceType.PAWN, PieceColor.BLACK))
                board.setPiece(Square(f, 7), Piece(backRank[f], PieceColor.BLACK))
            }
            return board
        }
    }
}
