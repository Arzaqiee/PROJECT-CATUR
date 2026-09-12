package com.qie.chess.model

enum class AppLanguage(val code: String, val label: String) {
    INDONESIAN("id", "Bahasa Indonesia"),
    ENGLISH("en", "English")
}

enum class BoardTheme(val label: String) {
    CLASSIC_MONO("Classic Mono"),
    SLATE("Slate"),
    WALNUT("Walnut")
}

data class AppSettings(
    val darkMode: Boolean = true,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC_MONO,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showLegalMoves: Boolean = true,
    val showCoordinates: Boolean = true,
    val animationsEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.INDONESIAN
)
