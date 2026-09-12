package com.qie.chess.multiplayer

import com.qie.chess.model.PieceColor
import kotlinx.coroutines.flow.Flow

enum class ConnectionStatus { IDLE, CONNECTING, WAITING_FOR_OPPONENT, CONNECTED, RECONNECTING, DISCONNECTED, ERROR }

data class RoomState(
    val roomCode: String,
    val status: ConnectionStatus,
    val hostColor: PieceColor = PieceColor.WHITE,
    val fen: String? = null,
    val lastMoveUci: String? = null,
    val gameFinished: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Abstraction over the realtime backend so the UI/ViewModel never depends
 * directly on Firebase. Swap the implementation (see [MultiplayerConfig])
 * without touching any screen code.
 */
interface OnlineGameConnection {
    val roomState: Flow<RoomState>

    suspend fun createRoom(): String
    suspend fun joinRoom(code: String)
    suspend fun sendMove(uciMove: String, resultingFen: String)
    suspend fun resign()
    suspend fun leaveRoom()
}
