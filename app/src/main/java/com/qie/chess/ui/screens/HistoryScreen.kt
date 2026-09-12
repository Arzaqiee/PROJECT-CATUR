package com.qie.chess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qie.chess.model.GameHistoryEntry
import com.qie.chess.model.GameMode
import com.qie.chess.model.GameResult
import com.qie.chess.model.PieceColor
import com.qie.chess.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit) {
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (entries.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Clear history")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No games yet \u2014 play one to see it here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                items(entries) { entry -> HistoryRow(entry) }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: GameHistoryEntry) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modeLabel(entry.mode), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(dateFormat.format(Date(entry.dateEpochMillis)), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = resultLabel(entry.result) + " \u00B7 vs ${entry.opponentLabel} \u00B7 You played " +
                (if (entry.playerColor == PieceColor.WHITE) "White" else "Black") + " \u00B7 ${entry.moveCount} moves",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

private fun modeLabel(mode: GameMode): String = when (mode) {
    GameMode.VS_BOT -> "vs Bot"
    GameMode.VS_FRIEND_ONLINE -> "vs Friend (Online)"
    GameMode.LOCAL_TWO_PLAYER -> "Local 2 Player"
    GameMode.PUZZLE -> "Puzzle"
}

private fun resultLabel(result: GameResult): String = when (result) {
    GameResult.WHITE_WINS_CHECKMATE, GameResult.WHITE_WINS_RESIGN, GameResult.WHITE_WINS_TIMEOUT -> "White won"
    GameResult.BLACK_WINS_CHECKMATE, GameResult.BLACK_WINS_RESIGN, GameResult.BLACK_WINS_TIMEOUT -> "Black won"
    else -> "Draw"
}
