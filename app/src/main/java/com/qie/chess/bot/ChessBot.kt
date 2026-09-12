package com.qie.chess.bot

import com.qie.chess.engine.ChessEngine
import com.qie.chess.model.GameState
import com.qie.chess.model.Move
import com.qie.chess.model.PieceColor
import kotlin.random.Random

/**
 * A real, if lightweight, chess-playing engine: minimax search with
 * alpha-beta pruning over [BotDifficulty.searchDepth] plies, using
 * [Evaluator] as the leaf heuristic. Not Stockfish-strength, but it
 * actually understands material, development and king safety, and will
 * find short tactics/mates within its search depth.
 *
 * To swap in a native engine (e.g. Stockfish via JNI) later, just replace
 * the body of [findBestMove] - the public API can stay the same.
 */
class ChessBot(private val difficulty: BotDifficulty) {

    private val random = Random(System.currentTimeMillis())

    /** Picks a move for the side to move in [state]. Call off the main thread. */
    fun findBestMove(state: GameState): Move? {
        val legalMoves = ChessEngine.legalMoves(state)
        if (legalMoves.isEmpty()) return null
        if (legalMoves.size == 1) return legalMoves.first()

        val maximizing = state.sideToMove == PieceColor.WHITE
        val scored = legalMoves.map { move ->
            val next = ChessEngine.makeMove(state, move)
            val score = minimax(
                next,
                depth = difficulty.searchDepth - 1,
                alpha = Int.MIN_VALUE / 2,
                beta = Int.MAX_VALUE / 2,
                maximizing = !maximizing
            )
            move to score
        }

        val best = if (maximizing) scored.maxOf { it.second } else scored.minOf { it.second }

        // Within the difficulty's "blunder window", pick randomly among near-best moves
        // so Easy/Medium feel human rather than perfectly deterministic.
        val window = difficulty.randomnessCentipawns
        val candidates = scored.filter { (_, score) ->
            if (maximizing) score >= best - window else score <= best + window
        }
        return candidates[random.nextInt(candidates.size)].first
    }

    private fun minimax(state: GameState, depth: Int, alpha: Int, beta: Int, maximizing: Boolean): Int {
        if (state.result.isGameOver) {
            return terminalScore(state, depth)
        }
        if (depth == 0) {
            return Evaluator.evaluate(state)
        }

        val moves = ChessEngine.legalMoves(state)
        var a = alpha
        var b = beta

        if (maximizing) {
            var value = Int.MIN_VALUE / 2
            for (move in moves) {
                val next = ChessEngine.makeMove(state, move)
                value = maxOf(value, minimax(next, depth - 1, a, b, false))
                a = maxOf(a, value)
                if (b <= a) break
            }
            return value
        } else {
            var value = Int.MAX_VALUE / 2
            for (move in moves) {
                val next = ChessEngine.makeMove(state, move)
                value = minOf(value, minimax(next, depth - 1, a, b, true))
                b = minOf(b, value)
                if (b <= a) break
            }
            return value
        }
    }

    private fun terminalScore(state: GameState, depthRemaining: Int): Int {
        // Prefer faster mates / slower losses by weighting remaining depth.
        return when (state.result) {
            com.qie.chess.model.GameResult.WHITE_WINS_CHECKMATE -> 1_000_000 + depthRemaining
            com.qie.chess.model.GameResult.BLACK_WINS_CHECKMATE -> -1_000_000 - depthRemaining
            else -> 0 // stalemate / draws
        }
    }
}
