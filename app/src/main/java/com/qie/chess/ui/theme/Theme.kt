package com.qie.chess.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class BoardColors(
    val light: Color,
    val dark: Color,
    val lastMove: Color,
    val legalDot: Color,
    val selected: Color,
    val check: Color
)

val LocalBoardColors = staticCompositionLocalOf {
    BoardColors(
        light = BoardLightSquare,
        dark = BoardDarkSquare,
        lastMove = BoardLastMove,
        legalDot = BoardLegalDot,
        selected = BoardSelected,
        check = BoardCheck
    )
}

private val DarkPalette = darkColorScheme(
    primary = AccentGold,
    onPrimary = Ink900,
    background = Ink900,
    onBackground = TextPrimaryDark,
    surface = Ink800,
    onSurface = TextPrimaryDark,
    surfaceVariant = Ink700,
    onSurfaceVariant = TextSecondaryDark,
    outline = Ink600,
    error = DangerRed
)

private val LightPalette = lightColorScheme(
    primary = AccentGold,
    onPrimary = Paper100,
    background = Paper100,
    onBackground = TextPrimaryLight,
    surface = Paper200,
    onSurface = TextPrimaryLight,
    surfaceVariant = Paper200,
    onSurfaceVariant = TextSecondaryLight,
    outline = Paper300,
    error = DangerRed
)

@Composable
fun QieChessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkPalette else LightPalette
    MaterialTheme(
        colorScheme = colorScheme,
        typography = QieTypography,
        content = content
    )
}
