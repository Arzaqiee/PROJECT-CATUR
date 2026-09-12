package com.qie.chess.multiplayer

import kotlin.random.Random

object RoomManager {
    private val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no 0/O/1/I to avoid confusion

    fun generateRoomCode(length: Int = 6): String {
        return (1..length).map { CHARS[Random.nextInt(CHARS.length)] }.joinToString("")
    }
}
