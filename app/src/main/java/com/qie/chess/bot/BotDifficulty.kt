package com.qie.chess.bot

enum class BotDifficulty(val label: String, val searchDepth: Int, val randomnessCentipawns: Int) {
    EASY("Easy", 1, 120),
    MEDIUM("Medium", 2, 50),
    HARD("Hard", 3, 0)
}
