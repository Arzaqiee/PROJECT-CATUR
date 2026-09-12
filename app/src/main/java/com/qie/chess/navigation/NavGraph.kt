package com.qie.chess.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qie.chess.QieChessApp
import com.qie.chess.data.PuzzleRepository
import com.qie.chess.model.GameMode
import com.qie.chess.ui.screens.*
import com.qie.chess.viewmodel.GameViewModel
import com.qie.chess.viewmodel.HistoryViewModel
import com.qie.chess.viewmodel.QieViewModelFactory
import com.qie.chess.viewmodel.SettingsViewModel

@Composable
fun QieNavGraph(app: QieChessApp) {
    val navController: NavHostController = rememberNavController()
    val factory = remember { QieViewModelFactory(app) }

    val gameViewModel: GameViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)

    val settings by settingsViewModel.settings.collectAsState()

    // Keep sound/vibration engines in sync with user settings.
    LaunchedEffect(settings.soundEnabled) { app.soundManager.setEnabled(settings.soundEnabled) }
    LaunchedEffect(settings.vibrationEnabled) { app.haptics.setEnabled(settings.vibrationEnabled) }

    NavHost(navController = navController, startDestination = Routes.MAIN_MENU) {

        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                onPlayVsBot = { navController.navigate(Routes.BOT_SETUP) },
                onPlayOnline = { navController.navigate(Routes.ONLINE_LOBBY) },
                onPlayLocal = { navController.navigate(Routes.LOCAL_SETUP) },
                onPuzzles = { navController.navigate(Routes.PUZZLE_LIST) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.BOT_SETUP) {
            BotSetupScreen(
                onBack = { navController.popBackStack() },
                onStart = { color, difficulty, timeControl ->
                    gameViewModel.startNewGame(GameMode.VS_BOT, color, difficulty, timeControl)
                    navController.navigate(Routes.GAME)
                }
            )
        }

        composable(Routes.LOCAL_SETUP) {
            LocalSetupScreen(
                onBack = { navController.popBackStack() },
                onStart = { timeControl ->
                    gameViewModel.startNewGame(GameMode.LOCAL_TWO_PLAYER, timeControl = timeControl)
                    navController.navigate(Routes.GAME)
                }
            )
        }

        composable(Routes.ONLINE_LOBBY) {
            OnlineLobbyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                showLegalMoves = settings.showLegalMoves,
                showCoordinates = settings.showCoordinates,
                onExitToMenu = {
                    navController.popBackStack(Routes.MAIN_MENU, inclusive = false)
                }
            )
        }

        composable(Routes.PUZZLE_LIST) {
            PuzzleListScreen(
                onBack = { navController.popBackStack() },
                onSelectPuzzle = { puzzle ->
                    navController.navigate("${Routes.PUZZLE_GAME}/${puzzle.id}")
                }
            )
        }

        composable("${Routes.PUZZLE_GAME}/{puzzleId}") { backStackEntry ->
            val puzzleId = backStackEntry.arguments?.getString("puzzleId")
            val puzzle = PuzzleRepository.byId(puzzleId ?: "") ?: PuzzleRepository.puzzles.first()
            PuzzlePlayScreen(
                puzzle = puzzle,
                viewModel = gameViewModel,
                onBack = { navController.popBackStack(Routes.PUZZLE_LIST, inclusive = false) },
                onNextPuzzle = {
                    val currentIndex = PuzzleRepository.puzzles.indexOfFirst { it.id == puzzle.id }
                    val next = PuzzleRepository.puzzles.getOrNull(currentIndex + 1) ?: PuzzleRepository.puzzles.first()
                    navController.navigate("${Routes.PUZZLE_GAME}/${next.id}") {
                        popUpTo(Routes.PUZZLE_LIST)
                    }
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(viewModel = historyViewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(viewModel = settingsViewModel, onBack = { navController.popBackStack() })
        }
    }
}
