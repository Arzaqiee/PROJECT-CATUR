package com.qie.chess.model

enum class GameResult {
    IN_PROGRESS,
    WHITE_WINS_CHECKMATE,
    BLACK_WINS_CHECKMATE,
    WHITE_WINS_RESIGN,
    BLACK_WINS_RESIGN,
    WHITE_WINS_TIMEOUT,
    BLACK_WINS_TIMEOUT,
    DRAW_STALEMATE,
    DRAW_THREEFOLD_REPETITION,
    DRAW_FIFTY_MOVE_RULE,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_AGREEMENT;

    val isGameOver: Boolean get() = this != IN_PROGRESS

    val isDraw: Boolean get() = name.startsWith("DRAW")
}
