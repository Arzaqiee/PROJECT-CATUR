package com.qie.chess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qie.chess.data.PuzzleRepository
import com.qie.chess.model.Puzzle
import com.qie.chess.ui.components.ChessBoardView
import com.qie.chess.ui.components.MenuButton
import com.qie.chess.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleListScreen(onBack: () -> Unit, onSelectPuzzle: (Puzzle) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Puzzles") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            items(PuzzleRepository.puzzles) { puzzle ->
                MenuButton(
                    title = puzzle.title,
                    subtitle = puzzle.description,
                    onClick = { onSelectPuzzle(puzzle) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzlePlayScreen(
    puzzle: Puzzle,
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNextPuzzle: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var feedback by remember(puzzle.id) { mutableStateOf<String?>(null) }
    var solved by remember(puzzle.id) { mutableStateOf(false) }

    LaunchedEffect(puzzle.id) {
        viewModel.loadPuzzle(puzzle)
        feedback = null
        solved = false
    }

    // Check the last move played against the expected solution move.
    LaunchedEffect(state.gameState.moveHistory.size) {
        val lastMove = state.gameState.moveHistory.lastOrNull() ?: return@LaunchedEffect
        val expected = puzzle.solutionMoves.getOrNull(0)
        if (expected != null && lastMove.toLongAlgebraic().startsWith(expected)) {
            feedback = "Correct! \u2713"
            solved = true
        } else if (feedback == null) {
            feedback = "Not quite \u2014 try again \u2717"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(puzzle.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Text(puzzle.description, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))

            ChessBoardView(
                gameState = state.gameState,
                selectedSquare = state.selectedSquare,
                legalMoves = state.legalMovesForSelected,
                lastMove = state.lastMove,
                flipped = state.boardFlipped,
                showCoordinates = true,
                showLegalMoves = true,
                onSquareTapped = viewModel::onSquareTapped,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            feedback?.let {
                Text(
                    text = it,
                    color = if (solved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { viewModel.loadPuzzle(puzzle); feedback = null; solved = false },
                    modifier = Modifier.weight(1f)
                ) { Text("Reset") }
                Button(
                    onClick = onNextPuzzle,
                    modifier = Modifier.weight(1f)
                ) { Text("Next Puzzle") }
            }
        }
    }
}
