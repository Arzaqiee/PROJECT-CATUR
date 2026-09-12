package com.qie.chess.model

/**
 * A board square. file/rank are 0-based: file 0 = 'a', rank 0 = rank '1'.
 */
data class Square(val file: Int, val rank: Int) {

    val isValid: Boolean get() = file in 0..7 && rank in 0..7

    /** Algebraic notation, e.g. "e4". */
    override fun toString(): String {
        val fileChar = ('a' + file)
        val rankChar = ('1' + rank)
        return "$fileChar$rankChar"
    }

    companion object {
        fun fromAlgebraic(s: String): Square {
            require(s.length == 2) { "Invalid square: $s" }
            val file = s[0] - 'a'
            val rank = s[1] - '1'
            return Square(file, rank)
        }
    }
}
