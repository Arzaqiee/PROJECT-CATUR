package com.qie.chess.model

/**
 * Chess clock configuration. [initialMinutes] = starting minutes per side,
 * [incrementSeconds] = Fischer increment added after each move. A time
 * control with initialMinutes == 0 means "no timer".
 */
data class TimeControl(
    val label: String,
    val initialMinutes: Int,
    val incrementSeconds: Int
) {
    val initialMillis: Long get() = initialMinutes * 60_000L
    val incrementMillis: Long get() = incrementSeconds * 1000L

    companion object {
        val NONE = TimeControl("No timer", 0, 0)
        val PRESETS = listOf(
            TimeControl("1 + 0", 1, 0),
            TimeControl("3 + 0", 3, 0),
            TimeControl("3 + 2", 3, 2),
            TimeControl("5 + 0", 5, 0),
            TimeControl("10 + 0", 10, 0),
            TimeControl("10 + 5", 10, 5),
            TimeControl("15 + 10", 15, 10),
            NONE
        )
    }
}
