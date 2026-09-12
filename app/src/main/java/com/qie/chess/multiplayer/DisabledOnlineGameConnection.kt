package com.qie.chess.multiplayer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Active whenever [MultiplayerConfig.FIREBASE_CONFIGURED] is false. */
class DisabledOnlineGameConnection : OnlineGameConnection {

    private val state = MutableStateFlow(
        RoomState(
            roomCode = "",
            status = ConnectionStatus.ERROR,
            errorMessage = "Online multiplayer belum dikonfigurasi. Lihat README.md untuk setup Firebase."
        )
    )
    override val roomState = state.asStateFlow()

    override suspend fun createRoom(): String = ""
    override suspend fun joinRoom(code: String) {}
    override suspend fun sendMove(uciMove: String, resultingFen: String) {}
    override suspend fun resign() {}
    override suspend fun leaveRoom() {}
}
