package com.qie.chess.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qie.chess.engine.ChessEngine
import com.qie.chess.model.*
import com.qie.chess.ui.theme.LocalBoardColors

@Composable
fun ChessBoardView(
    gameState: GameState,
    selectedSquare: Square?,
    legalMoves: List<Move>,
    lastMove: Move?,
    flipped: Boolean,
    showCoordinates: Boolean,
    showLegalMoves: Boolean,
    onSquareTapped: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardColors = LocalBoardColors.current
    val kingInCheckSquare: Square? = if (ChessEngine.isInCheck(gameState, gameState.sideToMove)) {
        gameState.board.findKing(gameState.sideToMove)
    } else null

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
    ) {
        val ranks = if (flipped) (0..7) else (7 downTo 0)
        for (rank in ranks) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val files = if (flipped) (7 downTo 0) else (0..7)
                for (file in files) {
                    val square = Square(file, rank)
                    val piece = gameState.board.pieceAt(square)
                    val isLight = (file + rank) % 2 == 1
                    val isSelected = square == selectedSquare
                    val isLastMove = lastMove != null && (square == lastMove.from || square == lastMove.to)
                    val isCheck = square == kingInCheckSquare
                    val legalTarget = legalMoves.firstOrNull { it.to == square }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isLight) boardColors.light else boardColors.dark)
                            .then(
                                if (isSelected) Modifier.background(boardColors.selected)
                                else if (isCheck) Modifier.background(boardColors.check)
                                else if (isLastMove) Modifier.background(boardColors.lastMove)
                                else Modifier
                            )
                            .clickable { onSquareTapped(square) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (showCoordinates && file == (if (flipped) 7 else 0)) {
                            Text(
                                text = ('1' + rank).toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isLight) boardColors.dark else boardColors.light,
                                modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
                            )
                        }
                        if (showCoordinates && rank == (if (flipped) 7 else 0)) {
                            Text(
                                text = ('a' + file).toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isLight) boardColors.dark else boardColors.light,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                            )
                        }

                        if (piece != null) {
                            PieceGlyph(piece = piece, highlighted = isSelected)
                        }

                        if (showLegalMoves && legalTarget != null) {
                            LegalMoveMarker(isCapture = legalTarget.capturedPiece != null, color = boardColors.legalDot)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceGlyph(piece: Piece, highlighted: Boolean) {
    val scale by animateFloatAsState(targetValue = if (highlighted) 1.08f else 1f, animationSpec = tween(120), label = "pieceScale")
    Text(
        text = PieceGlyphs.glyph(piece),
        fontSize = (26 * scale).sp,
        color = if (piece.color == PieceColor.WHITE) Color(0xFFF7F7F5) else Color(0xFF17171A),
        modifier = Modifier
    )
}

@Composable
private fun BoxScope.LegalMoveMarker(isCapture: Boolean, color: Color) {
    if (isCapture) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.86f)
                .align(Alignment.Center)
                .border(BorderStroke(3.dp, color), shape = androidx.compose.foundation.shape.CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(0.28f)
                .align(Alignment.Center)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
    }
}
