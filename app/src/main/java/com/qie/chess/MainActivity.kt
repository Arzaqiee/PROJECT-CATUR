package com.qie.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qie.chess.navigation.QieNavGraph
import com.qie.chess.ui.theme.QieChessTheme
import com.qie.chess.viewmodel.QieViewModelFactory
import com.qie.chess.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as QieChessApp

        setContent {
            val factory = remember { QieViewModelFactory(app) }
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            val settings by settingsViewModel.settings.collectAsState()

            QieChessTheme(darkTheme = settings.darkMode) {
                QieNavGraph(app = app)
            }
        }
    }
}
