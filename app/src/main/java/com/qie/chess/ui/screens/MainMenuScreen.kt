package com.qie.chess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScrollimport androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qie.chess.ui.components.MenuButton
import com.qie.chess.ui.components.SectionLabel

@Composable
fun MainMenuScreen(
    onPlayVsBot: () -> Unit,
    onPlayOnline: () -> Unit,
    onPlayLocal: () -> Unit,
    onPuzzles: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Text(
                text = "QIE",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "CHESS",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.offset(y = (-10).dp)
            )

            Spacer(modifier = Modifier.height(36.dp))
            SectionLabel("Play")
            MenuButton(title = "Play vs Bot", subtitle = "Easy, Medium, or Hard", emphasized = true, onClick = onPlayVsBot)
            Spacer(modifier = Modifier.height(10.dp))
            MenuButton(title = "Play vs Friend", subtitle = "Online, via room code", onClick = onPlayOnline)
            Spacer(modifier = Modifier.height(10.dp))
            MenuButton(title = "Local 2 Player", subtitle = "Pass and play on one device", onClick = onPlayLocal)

            Spacer(modifier = Modifier.height(28.dp))
            SectionLabel("More")
            MenuButton(title = "Puzzles", subtitle = "Sharpen your tactics", onClick = onPuzzles)
            Spacer(modifier = Modifier.height(10.dp))
            MenuButton(title = "History", subtitle = "Your past games", onClick = onHistory)
            Spacer(modifier = Modifier.height(10.dp))
            MenuButton(title = "Settings", onClick = onSettings)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

