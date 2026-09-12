package com.qie.chess.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qie.chess.model.AppLanguage
import com.qie.chess.model.AppSettings
import com.qie.chess.model.BoardTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "qie_chess_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val SHOW_LEGAL_MOVES = booleanPreferencesKey("show_legal_moves")
        val SHOW_COORDINATES = booleanPreferencesKey("show_coordinates")
        val ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            darkMode = prefs[Keys.DARK_MODE] ?: true,
            boardTheme = prefs[Keys.BOARD_THEME]?.let { name ->
                runCatching { BoardTheme.valueOf(name) }.getOrNull()
            } ?: BoardTheme.CLASSIC_MONO,
            soundEnabled = prefs[Keys.SOUND] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
            showLegalMoves = prefs[Keys.SHOW_LEGAL_MOVES] ?: true,
            showCoordinates = prefs[Keys.SHOW_COORDINATES] ?: true,
            animationsEnabled = prefs[Keys.ANIMATIONS] ?: true,
            language = prefs[Keys.LANGUAGE]?.let { code ->
                AppLanguage.entries.firstOrNull { it.code == code }
            } ?: AppLanguage.INDONESIAN
        )
    }

    suspend fun update(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = settings.darkMode
            prefs[Keys.BOARD_THEME] = settings.boardTheme.name
            prefs[Keys.SOUND] = settings.soundEnabled
            prefs[Keys.VIBRATION] = settings.vibrationEnabled
            prefs[Keys.SHOW_LEGAL_MOVES] = settings.showLegalMoves
            prefs[Keys.SHOW_COORDINATES] = settings.showCoordinates
            prefs[Keys.ANIMATIONS] = settings.animationsEnabled
            prefs[Keys.LANGUAGE] = settings.language.code
        }
    }
}
