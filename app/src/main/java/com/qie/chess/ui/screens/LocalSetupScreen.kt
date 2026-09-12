package com.qie.chess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qie.chess.model.TimeControl
import com.qie.chess.ui.components.ChoiceChip
import com.qie.chess.ui.components.SectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalSetupScreen(
    onBack: () -> Unit,
    onStart: (TimeControl) -> Unit
) {
    var timeControl by remember { mutableStateOf(TimeControl.NONE) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Local 2 Player") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {
            Text(
                "Two players share this device, taking turns on the same board.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            SectionLabel("Time control")
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(120.dp)) {
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
                onClick = { onStart(timeControl) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Start Game")
            }
        }
    }
}
