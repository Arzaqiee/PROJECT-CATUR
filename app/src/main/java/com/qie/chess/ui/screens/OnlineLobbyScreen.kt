package com.qie.chess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.qie.chess.multiplayer.MultiplayerConfig
import com.qie.chess.ui.components.SectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineLobbyScreen(onBack: () -> Unit) {
    var joinCode by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Play vs Friend") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {

            if (!MultiplayerConfig.FIREBASE_CONFIGURED) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Online multiplayer belum dikonfigurasi",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Fitur ini butuh backend Firebase gratis milikmu sendiri. " +
                                "Lihat README.md bagian \"Konfigurasi Firebase\" untuk langkah setup " +
                                "(gratis, ~10 menit). Setelah itu, layar ini otomatis aktif penuh.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            SectionLabel("Create a room")
            Button(
                onClick = { /* wired to OnlineGameConnection.createRoom() once Firebase is configured */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = MultiplayerConfig.FIREBASE_CONFIGURED
            ) {
                Text("Create Room")
            }

            Spacer(modifier = Modifier.height(28.dp))
            SectionLabel("Join a room")
            OutlinedTextField(
                value = joinCode,
                onValueChange = { if (it.length <= 6) joinCode = it.uppercase() },
                label = { Text("Room code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { /* wired to OnlineGameConnection.joinRoom(joinCode) once Firebase is configured */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = MultiplayerConfig.FIREBASE_CONFIGURED && joinCode.length == 6
            ) {
                Text("Join Room")
            }
        }
    }
}
