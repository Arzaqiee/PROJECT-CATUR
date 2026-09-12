package com.qie.chess.engine

import com.qie.chess.model.*

/**
 * The single source of truth for chess rules: legal move generation,
 * check/checkmate/stalemate detection, applying a move, and draw rules
 * (fifty-move, threefold repetition, insufficient material).
 */
object ChessEngine {

    /** True if [square] is attacked by any piece of [byColor] on [board]. */
    fun isSquareAttacked(board: Board, square: Square, byColor: PieceColor): Boolean {
        // Pawn attacks
        val pawnDir = if (byColor == PieceColor.WHITE) -1 else 1
        for (df in intArrayOf(-1, 1)) {
            val s = Square(square.file + df, square.rank + pawnDir)
            if (s.isValid) {
                val p = board.pieceAt(s)
                if (p != null && p.color == byColor && p.type == PieceType.PAWN) return true
            }
        }
        // Knight attacks
        val knightOffsets = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
        for ((df, dr) in knightOffsets) {
            val s = Square(square.file + df, square.rank + dr)
            if (s.isValid) {
                val p = board.pieceAt(s)
                if (p != null && p.color == byColor && p.type == PieceType.KNIGHT) return true
            }
        }
        // King attacks (adjacent)
        for (df in -1..1) for (dr in -1..1) {
            if (df == 0 && dr == 0) continue
            val s = Square(square.file + df, square.rank + dr)
            if (s.isValid) {
                val p = board.pieceAt(s)
                if (p != null && p.color == byColor && p.type == PieceType.KING) return true
            }
        }
        // Sliding: bishop/queen diagonals
        val diagDirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        for ((df, dr) in diagDirs) {
            var s = Square(square.file + df, square.rank + dr)
            while (s.isValid) {
                val p = board.pieceAt(s)
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) return true
                    break
                }
                s = Square(s.file + df, s.rank + dr)
            }
        }
        // Sliding: rook/queen straight lines
        val straightDirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        for ((df, dr) in straightDirs) {
            var s = Square(square.file + df, square.rank + dr)
            while (s.isValid) {
                val p = board.pieceAt(s)
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) return true
                    break
                }
                s = Square(s.file + df, s.rank + dr)
            }
        }
        return false
    }

    fun isInCheck(state: GameState, color: PieceColor): Boolean {
        val kingSquare = state.board.findKing(color) ?: return false
        return isSquareAttacked(state.board, kingSquare, color.opposite())
    }

    /** All fully legal moves for the side to move. */
    fun legalMoves(state: GameState): List<Move> {
        val pseudo = MoveGenerator.allPseudoLegalMoves(state, state.sideToMove)
        return pseudo.filter { move -> !leavesKingInCheck(state, move) }
    }

    fun legalMovesFrom(state: GameState, square: Square): List<Move> {
        val piece = state.board.pieceAt(square) ?: return emptyList()
        if (piece.color != state.sideToMove) return emptyList()
        return MoveGenerator.pseudoLegalMoves(state, square)
            .filter { move -> !leavesKingInCheck(state, move) }
    }

    private fun leavesKingInCheck(state: GameState, move: Move): Boolean {
        val after = applyMoveToBoard(state, move)
        return isSquareAttacked(
            after.board,
            after.board.findKing(state.sideToMove) ?: return true,
            state.sideToMove.opposite()
        )
    }

    /**
     * Applies [move] and returns the full resulting [GameState], including
     * updated castling rights, en passant target, clocks, history and
     * game-result detection (checkmate / stalemate / draws).
     *
     * Assumes [move] was obtained from [legalMoves] / [legalMovesFrom] (i.e. is legal).
     */
    fun makeMove(state: GameState, move: Move): GameState {
        var next = applyMoveToBoard(state, move)

        val isPawnMove = move.piece.type == PieceType.PAWN
        val isCapture = move.capturedPiece != null
        val halfmove = if (isPawnMove || isCapture) 0 else state.halfmoveClock + 1
        val fullmove = if (state.sideToMove == PieceColor.BLACK) state.fullmoveNumber + 1 else state.fullmoveNumber

        next = next.copy(
            sideToMove = state.sideToMove.opposite(),
            halfmoveClock = halfmove,
            fullmoveNumber = fullmove,
            moveHistory = state.moveHistory + move
        )
        next = next.copy(positionHistory = state.positionHistory + next.positionKey())
        next = next.copy(result = detectResult(next))
        return next
    }

    /** Applies board/castling/en-passant changes only; does not touch clocks/history/result. */
    private fun applyMoveToBoard(state: GameState, move: Move): GameState {
        val board = state.board.copy()
        var castling = state.castlingRights
        var enPassant: Square? = null

        // Move the piece
        board.setPiece(move.from, null)
        val movedPiece = if (move.flag == MoveFlag.PROMOTION && move.promotionType != null) {
            Piece(move.promotionType, move.piece.color)
        } else move.piece
        board.setPiece(move.to, movedPiece)

        // En passant capture removes the pawn behind the destination square
        if (move.flag == MoveFlag.EN_PASSANT) {
            val capturedRank = move.from.rank
            board.setPiece(Square(move.to.file, capturedRank), null)
        }

        // Castling also moves the rook
        if (move.flag == MoveFlag.CASTLE_KINGSIDE) {
            val rank = move.from.rank
            val rook = board.pieceAt(Square(7, rank))
            board.setPiece(Square(7, rank), null)
            board.setPiece(Square(5, rank), rook)
        } else if (move.flag == MoveFlag.CASTLE_QUEENSIDE) {
            val rank = move.from.rank
            val rook = board.pieceAt(Square(0, rank))
            board.setPiece(Square(0, rank), null)
            board.setPiece(Square(3, rank), rook)
        }

        // Track double pawn push for next en-passant opportunity
        if (move.flag == MoveFlag.DOUBLE_PAWN_PUSH) {
            val midRank = (move.from.rank + move.to.rank) / 2
            enPassant = Square(move.from.file, midRank)
        }

        // Update castling rights on king/rook moves or rook captures
        castling = updateCastlingRights(castling, move)

        return state.copy(board = board, castlingRights = castling, enPassantTarget = enPassant)
    }

    private fun updateCastlingRights(rights: CastlingRights, move: Move): CastlingRights {
        var r = rights
        // King moved
        if (move.piece.type == PieceType.KING) {
            r = if (move.piece.color == PieceColor.WHITE) {
                r.copy(whiteKingSide = false, whiteQueenSide = false)
            } else {
                r.copy(blackKingSide = false, blackQueenSide = false)
            }
        }
        // Rook moved from its home square
        when (move.from) {
            Square(0, 0) -> r = r.copy(whiteQueenSide = false)
            Square(7, 0) -> r = r.copy(whiteKingSide = false)
            Square(0, 7) -> r = r.copy(blackQueenSide = false)
            Square(7, 7) -> r = r.copy(blackKingSide = false)
            else -> {}
        }
        // Rook captured on its home square
        when (move.to) {
            Square(0, 0) -> r = r.copy(whiteQueenSide = false)
            Square(7, 0) -> r = r.copy(whiteKingSide = false)
            Square(0, 7) -> r = r.copy(blackQueenSide = false)
            Square(7, 7) -> r = r.copy(blackKingSide = false)
            else -> {}
        }
        return r
    }

    private fun detectResult(state: GameState): GameResult {
        val sideToMove = state.sideToMove
        val hasLegalMove = legalMoves(state).isNotEmpty()
        val inCheck = isInCheck(state, sideToMove)

        if (!hasLegalMove) {
            return if (inCheck) {
                if (sideToMove == PieceColor.WHITE) GameResult.BLACK_WINS_CHECKMATE else GameResult.WHITE_WINS_CHECKMATE
            } else {
                GameResult.DRAW_STALEMATE
            }
        }
        if (state.halfmoveClock >= 100) return GameResult.DRAW_FIFTY_MOVE_RULE
        if (state.repetitionCount() >= 3) return GameResult.DRAW_THREEFOLD_REPETITION
        if (isInsufficientMaterial(state.board)) return GameResult.DRAW_INSUFFICIENT_MATERIAL
        return GameResult.IN_PROGRESS
    }

    private fun isInsufficientMaterial(board: Board): Boolean {
        val pieces = board.allPieces().map { it.second }
        if (pieces.any { it.type == PieceType.PAWN || it.type == PieceType.ROOK || it.type == PieceType.QUEEN }) {
            return false
        }
        // Only kings, and possibly knights/bishops left
        val minor = pieces.filter { it.type == PieceType.KNIGHT || it.type == PieceType.BISHOP }
        return minor.size <= 1
    }
}
