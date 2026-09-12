package com.qie.chess.model

enum class GameMode {
    VS_BOT,
    VS_FRIEND_ONLINE,
    LOCAL_TWO_PLAYER,
    PUZZLE
}

enum class PlayerColorChoice(val label: String) {
    WHITE("White"),
    BLACK("Black"),
    RANDOM("Random")
}
