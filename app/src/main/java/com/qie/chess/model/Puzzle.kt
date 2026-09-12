package com.qie.chess.model

/**
 * A simple tactical puzzle: a starting FEN, the side to move, and the
 * expected best move(s) in long-algebraic form (e.g. "e2e4", "e7e8q").
 * If the puzzle is a short forced sequence, [solutionMoves] lists every
 * ply of the expected line (player move, then bot reply, etc).
 */
data class Puzzle(
    val id: String,
    val title: String,
    val fen: String,
    val sideToMove: PieceColor,
    val solutionMoves: List<String>,
    val description: String
)
