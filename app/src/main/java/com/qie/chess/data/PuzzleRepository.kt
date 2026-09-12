package com.qie.chess.data

import com.qie.chess.model.PieceColor
import com.qie.chess.model.Puzzle

/**
 * Ships a small local puzzle set so Puzzles works fully offline on first run.
 * Add more by appending to [puzzles] - each is a FEN + expected first move
 * (extend solutionMoves with the opponent's forced reply + your follow-up
 * for multi-move tactics).
 */
object PuzzleRepository {

    val puzzles: List<Puzzle> = listOf(
        Puzzle(
            id = "p1",
            title = "Mate in 1",
            fen = "6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1",
            sideToMove = PieceColor.WHITE,
            solutionMoves = listOf("e1e8"),
            description = "Back-rank mate: find the rook move that ends the game immediately."
        ),
        Puzzle(
            id = "p2",
            title = "Fork the King and Rook",
            fen = "r3k2r/pppq1ppp/2n5/3p4/3P4/2N5/PPPQ1PPP/R3K2R w - - 4 9",
            sideToMove = PieceColor.WHITE,
            solutionMoves = listOf("c3d5"),
            description = "A knight jump wins material by attacking two pieces at once."
        ),
        Puzzle(
            id = "p3",
            title = "Mate in 1 (Queen)",
            fen = "7k/6pp/8/8/8/8/6PP/3Q2K1 w - - 0 1",
            sideToMove = PieceColor.WHITE,
            solutionMoves = listOf("d1d8"),
            description = "The king has no escape squares - deliver mate on the back rank."
        ),
        Puzzle(
            id = "p4",
            title = "Win the Queen",
            fen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/3P1N2/PPP2PPP/RNBQK2R b KQkq - 4 5",
            sideToMove = PieceColor.BLACK,
            solutionMoves = listOf("c5f2"),
            description = "Black spots an undefended weakness on f2."
        ),
        Puzzle(
            id = "p5",
            title = "Smothered Mate Setup",
            fen = "6rk/6pp/7N/8/8/8/8/6K1 w - - 0 1",
            sideToMove = PieceColor.WHITE,
            solutionMoves = listOf("h6f7"),
            description = "A quiet knight hop sets up unstoppable mate next move."
        )
    )

    fun byId(id: String): Puzzle? = puzzles.firstOrNull { it.id == id }
}
