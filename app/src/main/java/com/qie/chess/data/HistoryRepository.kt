package com.qie.chess.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qie.chess.model.GameHistoryEntry
import com.qie.chess.model.GameMode
import com.qie.chess.model.GameResult
import com.qie.chess.model.PieceColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.historyDataStore by preferencesDataStore(name = "qie_chess_history")

class HistoryRepository(private val context: Context) {

    private val key = stringPreferencesKey("entries_json")

    val historyFlow: Flow<List<GameHistoryEntry>> = context.historyDataStore.data.map { prefs ->
        decode(prefs[key] ?: "[]")
    }

    suspend fun addEntry(entry: GameHistoryEntry) {
        context.historyDataStore.edit { prefs ->
            val current = decode(prefs[key] ?: "[]").toMutableList()
            current.add(0, entry) // newest first
            prefs[key] = encode(current)
        }
    }

    suspend fun clear() {
        context.historyDataStore.edit { prefs -> prefs[key] = "[]" }
    }

    suspend fun currentEntries(): List<GameHistoryEntry> = historyFlow.first()

    private fun encode(entries: List<GameHistoryEntry>): String {
        val arr = JSONArray()
        for (e in entries) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("date", e.dateEpochMillis)
            obj.put("mode", e.mode.name)
            obj.put("result", e.result.name)
            obj.put("color", e.playerColor.name)
            obj.put("opponent", e.opponentLabel)
            obj.put("moves", e.moveCount)
            obj.put("pgn", e.pgn)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decode(json: String): List<GameHistoryEntry> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                GameHistoryEntry(
                    id = obj.getString("id"),
                    dateEpochMillis = obj.getLong("date"),
                    mode = GameMode.valueOf(obj.getString("mode")),
                    result = GameResult.valueOf(obj.getString("result")),
                    playerColor = PieceColor.valueOf(obj.getString("color")),
                    opponentLabel = obj.getString("opponent"),
                    moveCount = obj.getInt("moves"),
                    pgn = obj.optString("pgn", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
