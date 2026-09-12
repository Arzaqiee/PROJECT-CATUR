package com.qie.chess.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qie.chess.model.Piece
import com.qie.chess.model.PieceColor
import com.qie.chess.model.PieceType

@Composable
fun TimerChip(millis: Long, isActive: Boolean, modifier: Modifier = Modifier) {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val text = if (millis <= 0L && millis == 0L) "%d:%02d".format(minutes, seconds) else "%d:%02d".format(minutes, seconds)

    Box(
        modifier = modifier
            .background(
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CapturedPiecesRow(pieces: List<PieceType>, color: PieceColor, modifier: Modifier = Modifier) {
    if (pieces.isEmpty()) {
        Box(modifier = modifier.height(20.dp))
        return
    }
    Row(modifier = modifier) {
        pieces.sortedByDescending { it.value }.forEach { type ->
            Text(
                text = PieceGlyphs.glyph(Piece(type, color)),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 1.dp)
            )
        }
    }
}

@Composable
fun MoveHistoryPanel(sanMoves: List<String>, modifier: Modifier = Modifier) {
    val pairs = sanMoves.chunked(2)
    LazyColumn(modifier = modifier) {
        items(pairs.size) { index ->
            val pair = pairs[index]
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(
                    text = "${index + 1}.",
                    modifier = Modifier.width(28.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Text(
                    text = pair.getOrNull(0) ?: "",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
                Text(
                    text = pair.getOrNull(1) ?: "",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
            }
        }
    }
}
