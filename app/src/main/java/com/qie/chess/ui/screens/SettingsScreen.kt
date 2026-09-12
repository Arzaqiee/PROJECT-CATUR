package com.qie.chess.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qie.chess.model.AppLanguage
import com.qie.chess.model.BoardTheme
import com.qie.chess.ui.components.ChoiceChip
import com.qie.chess.ui.components.SectionLabel
import com.qie.chess.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            SectionLabel("Appearance")
            SettingsSwitchRow("Dark Mode", settings.darkMode) { viewModel.update { s -> s.copy(darkMode = it) } }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Board Theme", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            Row {
                BoardTheme.entries.forEach { theme ->
                    ChoiceChip(
                        label = theme.label,
                        selected = settings.boardTheme == theme,
                        onClick = { viewModel.update { s -> s.copy(boardTheme = theme) } },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("Board")
            SettingsSwitchRow("Show legal moves", settings.showLegalMoves) { viewModel.update { s -> s.copy(showLegalMoves = it) } }
            SettingsSwitchRow("Show coordinates", settings.showCoordinates) { viewModel.update { s -> s.copy(showCoordinates = it) } }
            SettingsSwitchRow("Animation", settings.animationsEnabled) { viewModel.update { s -> s.copy(animationsEnabled = it) } }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("Feedback")
            SettingsSwitchRow("Sound", settings.soundEnabled) { viewModel.update { s -> s.copy(soundEnabled = it) } }
            SettingsSwitchRow("Vibration", settings.vibrationEnabled) { viewModel.update { s -> s.copy(vibrationEnabled = it) } }

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("Language")
            Row {
                AppLanguage.entries.forEach { lang ->
                    ChoiceChip(
                        label = lang.label,
                        selected = settings.language == lang,
                        onClick = { viewModel.update { s -> s.copy(language = lang) } },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
