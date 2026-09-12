package com.qie.chess.engine

import com.qie.chess.model.*

/**
 * Generates pseudo-legal moves: obeys how each piece moves and blocks on
 * friendly pieces, but does NOT verify the move leaves the mover's own king
 * safe. [ChessEngine] filters these down to fully legal moves.
 */
object MoveGenerator {

    private val KNIGHT_OFFSETS = listOf(
        -2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1
    )
    private val KING_OFFSETS = listOf(
        -1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1
    )
    private val BISHOP_DIRS = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    private val ROOK_DIRS = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

    fun pseudoLegalMoves(state: GameState, from: Square): List<Move> {
        val piece = state.board.pieceAt(from) ?: return emptyList()
        return when (piece.type) {
            PieceType.PAWN -> pawnMoves(state, from, piece)
            PieceType.KNIGHT -> stepMoves(state, from, piece, KNIGHT_OFFSETS)
            PieceType.BISHOP -> slidingMoves(state, from, piece, BISHOP_DIRS)
            PieceType.ROOK -> slidingMoves(state, from, piece, ROOK_DIRS)
            PieceType.QUEEN -> slidingMoves(state, from, piece, BISHOP_DIRS + ROOK_DIRS)
            PieceType.KING -> kingMoves(state, from, piece)
        }
    }

    fun allPseudoLegalMoves(state: GameState, color: PieceColor): List<Move> {
        val moves = mutableListOf<Move>()
        for ((square, piece) in state.board.allPieces()) {
            if (piece.color == color) {
                moves.addAll(pseudoLegalMoves(state, square))
            }
        }
        return moves
    }

    private fun stepMoves(
        state: GameState,
        from: Square,
        piece: Piece,
        offsets: List<Pair<Int, Int>>
    ): List<Move> {
        val moves = mutableListOf<Move>()
        for ((df, dr) in offsets) {
            val to = Square(from.file + df, from.rank + dr)
            if (!to.isValid) continue
            val target = state.board.pieceAt(to)
            if (target == null || target.color != piece.color) {
                moves.add(Move(from, to, piece, capturedPiece = target))
            }
        }
        return moves
    }

    private fun slidingMoves(
        state: GameState,
        from: Square,
        piece: Piece,
        dirs: List<Pair<Int, Int>>
    ): List<Move> {
        val moves = mutableListOf<Move>()
        for ((df, dr) in dirs) {
            var to = Square(from.file + df, from.rank + dr)
            while (to.isValid) {
                val target = state.board.pieceAt(to)
                if (target == null) {
                    moves.add(Move(from, to, piece))
                } else {
                    if (target.color != piece.color) {
                        moves.add(Move(from, to, piece, capturedPiece = target))
                    }
                    break
                }
                to = Square(to.file + df, to.rank + dr)
            }
        }
        return moves
    }

    private fun kingMoves(state: GameState, from: Square, piece: Piece): List<Move> {
        val moves = stepMoves(state, from, piece, KING_OFFSETS).toMutableList()

        // Castling
        val color = piece.color
        val rank = if (color == PieceColor.WHITE) 0 else 7
        if (from == Square(4, rank)) {
            val rights = state.castlingRights
            val kingSideAllowed = if (color == PieceColor.WHITE) rights.whiteKingSide else rights.blackKingSide
            val queenSideAllowed = if (color == PieceColor.WHITE) rights.whiteQueenSide else rights.blackQueenSide

            if (kingSideAllowed &&
                state.board.pieceAt(Square(5, rank)) == null &&
                state.board.pieceAt(Square(6, rank)) == null &&
                state.board.pieceAt(Square(7, rank))?.let { it.type == PieceType.ROOK && it.color == color } == true &&
                !ChessEngine.isSquareAttacked(state.board, Square(4, rank), color.opposite()) &&
                !ChessEngine.isSquareAttacked(state.board, Square(5, rank), color.opposite()) &&
                !ChessEngine.isSquareAttacked(state.board, Square(6, rank), color.opposite())
            ) {
                moves.add(Move(from, Square(6, rank), piece, flag = MoveFlag.CASTLE_KINGSIDE))
            }

            if (queenSideAllowed &&
                state.board.pieceAt(Square(3, rank)) == null &&
                state.board.pieceAt(Square(2, rank)) == null &&
                state.board.pieceAt(Square(1, rank)) == null &&
                state.board.pieceAt(Square(0, rank))?.let { it.type == PieceType.ROOK && it.color == color } == true &&
                !ChessEngine.isSquareAttacked(state.board, Square(4, rank), color.opposite()) &&
                !ChessEngine.isSquareAttacked(state.board, Square(3, rank), color.opposite()) &&
                !ChessEngine.isSquareAttacked(state.board, Square(2, rank), color.opposite())
            ) {
                moves.add(Move(from, Square(2, rank), piece, flag = MoveFlag.CASTLE_QUEENSIDE))
            }
        }
        return moves
    }

    private fun pawnMoves(state: GameState, from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val dir = if (piece.color == PieceColor.WHITE) 1 else -1
        val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
        val promoRank = if (piece.color == PieceColor.WHITE) 7 else 0

        // Single push
        val oneStep = Square(from.file, from.rank + dir)
        if (oneStep.isValid && state.board.pieceAt(oneStep) == null) {
            addPawnMove(moves, from, oneStep, piece, promoRank, MoveFlag.NORMAL, null)

            // Double push
            if (from.rank == startRank) {
                val twoStep = Square(from.file, from.rank + 2 * dir)
                if (state.board.pieceAt(twoStep) == null) {
                    moves.add(Move(from, twoStep, piece, flag = MoveFlag.DOUBLE_PAWN_PUSH))
                }
            }
        }

        // Captures
        for (df in intArrayOf(-1, 1)) {
            val to = Square(from.file + df, from.rank + dir)
            if (!to.isValid) continue
            val target = state.board.pieceAt(to)
            if (target != null && target.color != piece.color) {
                addPawnMove(moves, from, to, piece, promoRank, MoveFlag.NORMAL, target)
            } else if (target == null && state.enPassantTarget == to) {
                moves.add(
                    Move(
                        from, to, piece, flag = MoveFlag.EN_PASSANT,
                        capturedPiece = Piece(PieceType.PAWN, piece.color.opposite())
                    )
                )
            }
        }
        return moves
    }

    private fun addPawnMove(
        moves: MutableList<Move>,
        from: Square,
        to: Square,
        piece: Piece,
        promoRank: Int,
        flagIfNotPromo: MoveFlag,
        captured: Piece?
    ) {
        if (to.rank == promoRank) {
            for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                moves.add(
                    Move(from, to, piece, flag = MoveFlag.PROMOTION, promotionType = promo, capturedPiece = captured)
                )
            }
        } else {
            moves.add(Move(from, to, piece, flag = flagIfNotPromo, capturedPiece = captured))
        }
    }
}
