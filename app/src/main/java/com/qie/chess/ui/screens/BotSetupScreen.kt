package com.qie.chess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qie.chess.bot.BotDifficulty
import com.qie.chess.model.PlayerColorChoice
import com.qie.chess.model.TimeControl
import com.qie.chess.ui.components.ChoiceChip
import com.qie.chess.ui.components.SectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotSetupScreen(
    onBack: () -> Unit,
    onStart: (PlayerColorChoice, BotDifficulty, TimeControl) -> Unit
) {
    var color by remember { mutableStateOf(PlayerColorChoice.WHITE) }
    var difficulty by remember { mutableStateOf(BotDifficulty.MEDIUM) }
    var timeControl by remember { mutableStateOf(TimeControl.NONE) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Play vs Bot") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            SectionLabel("Your color")
            Row {
                PlayerColorChoice.entries.forEach {
                    ChoiceChip(
                        label = it.label,
                        selected = color == it,
                        onClick = { color = it },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("Difficulty")
            Row {
                BotDifficulty.entries.forEach {
                    ChoiceChip(
                        label = it.label,
                        selected = difficulty == it,
                        onClick = { difficulty = it },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("Time control")
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(120.dp)
            ) {
                items(TimeControl.PRESETS) { tc ->
                    ChoiceChip(
                        label = tc.label,
                        selected = timeControl == tc,
                        onClick = { timeControl = tc },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onStart(color, difficulty, timeControl) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Start Game")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
