package com.qie.chess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qie.chess.model.GameMode
import com.qie.chess.model.PieceColor
import com.qie.chess.ui.components.*
import com.qie.chess.viewmodel.GameUiState
import com.qie.chess.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    showLegalMoves: Boolean,
    showCoordinates: Boolean,
    onExitToMenu: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Opponent row
            PlayerRow(
                label = opponentLabel(state),
                capturedPieces = if (state.playerColor == PieceColor.WHITE) state.capturedByBlack else state.capturedByWhite,
                capturedColor = state.playerColor.opposite(),
                timerMillis = if (state.playerColor == PieceColor.WHITE) state.blackTimeMillis else state.whiteTimeMillis,
                timerActive = state.gameState.sideToMove != state.playerColor,
                showTimer = state.timeControl.initialMinutes > 0,
                isThinking = state.isBotThinking
            )

            Spacer(modifier = Modifier.height(8.dp))

            ChessBoardView(
                gameState = state.gameState,
                selectedSquare = state.selectedSquare,
                legalMoves = state.legalMovesForSelected,
                lastMove = state.lastMove,
                flipped = state.boardFlipped,
                showCoordinates = showCoordinates,
                showLegalMoves = showLegalMoves,
                onSquareTapped = viewModel::onSquareTapped,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Player row (you)
            PlayerRow(
                label = "You",
                capturedPieces = if (state.playerColor == PieceColor.WHITE) state.capturedByWhite else state.capturedByBlack,
                capturedColor = state.playerColor,
                timerMillis = if (state.playerColor == PieceColor.WHITE) state.whiteTimeMillis else state.blackTimeMillis,
                timerActive = state.gameState.sideToMove == state.playerColor,
                showTimer = state.timeControl.initialMinutes > 0,
                isThinking = false
            )

            Spacer(modifier = Modifier.height(10.dp))

            MoveHistoryPanel(
                sanMoves = state.sanMoves,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.undoLastMove() },
                    modifier = Modifier.weight(1f),
                    enabled = state.mode != GameMode.VS_FRIEND_ONLINE && !state.isGameOver
                ) {
                    Icon(Icons.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Undo")
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(viewModel.copyPgn())) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Copy PGN")
                }
                Button(
                    onClick = { viewModel.resign() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !state.isGameOver
                ) {
                    Text("Resign")
                }
                OutlinedButton(
                    onClick = onExitToMenu,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Menu")
                }
            }
        }
    }

    state.pendingPromotion?.let { pending ->
        PromotionDialog(
            color = pending.color,
            onSelect = { viewModel.confirmPromotion(it) },
            onDismiss = { viewModel.cancelPromotion() }
        )
    }

    state.gameOverMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onExitToMenu,
            title = { Text("Game Over") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onExitToMenu) { Text("Back to Menu") }
            }
        )
    }
}

@Composable
private fun PlayerRow(
    label: String,
    capturedPieces: List<com.qie.chess.model.PieceType>,
    capturedColor: PieceColor,
    timerMillis: Long,
    timerActive: Boolean,
    showTimer: Boolean,
    isThinking: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (isThinking) "$label \u00B7 thinking\u2026" else label,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            CapturedPiecesRow(pieces = capturedPieces, color = capturedColor)
        }
        if (showTimer) {
            TimerChip(millis = timerMillis, isActive = timerActive)
        }
    }
}

private fun opponentLabel(state: GameUiState): String = when (state.mode) {
    GameMode.VS_BOT -> "Bot \u00B7 ${state.botDifficulty.label}"
    GameMode.VS_FRIEND_ONLINE -> "Friend"
    GameMode.LOCAL_TWO_PLAYER -> "Player 2"
    GameMode.PUZZLE -> "Puzzle"
}

