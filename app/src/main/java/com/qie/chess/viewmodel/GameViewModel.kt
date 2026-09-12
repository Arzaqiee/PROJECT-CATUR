package com.qie.chess.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qie.chess.audio.SoundEvent
import com.qie.chess.audio.SoundManager
import com.qie.chess.bot.BotDifficulty
import com.qie.chess.bot.ChessBot
import com.qie.chess.data.HistoryRepository
import com.qie.chess.engine.ChessEngine
import com.qie.chess.engine.Notation
import com.qie.chess.model.*
import com.qie.chess.util.HapticEvent
import com.qie.chess.util.Haptics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class GameViewModel(
    private val historyRepository: HistoryRepository,
    private val soundManager: SoundManager,
    private val haptics: Haptics
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var botJob: Job? = null

    fun startNewGame(
        mode: GameMode,
        colorChoice: PlayerColorChoice = PlayerColorChoice.WHITE,
        botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
        timeControl: TimeControl = TimeControl.NONE
    ) {
        timerJob?.cancel()
        botJob?.cancel()

        val playerColor = when (colorChoice) {
            PlayerColorChoice.WHITE -> PieceColor.WHITE
            PlayerColorChoice.BLACK -> PieceColor.BLACK
            PlayerColorChoice.RANDOM -> if (Random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK
        }

        _uiState.value = GameUiState(
            gameState = GameState.newGame(),
            mode = mode,
            playerColor = playerColor,
            botDifficulty = botDifficulty,
            timeControl = timeControl,
            whiteTimeMillis = timeControl.initialMillis,
            blackTimeMillis = timeControl.initialMillis,
            boardFlipped = mode == GameMode.VS_BOT && playerColor == PieceColor.BLACK
        )

        soundManager.play(SoundEvent.GAME_START)
        startClockIfNeeded()
        maybeTriggerBotMove()
    }

    fun loadPuzzle(puzzle: Puzzle) {
        timerJob?.cancel()
        botJob?.cancel()
        val state = com.qie.chess.engine.Fen.parse(puzzle.fen)
        _uiState.value = GameUiState(
            gameState = state,
            mode = GameMode.PUZZLE,
            playerColor = puzzle.sideToMove,
            timeControl = TimeControl.NONE,
            boardFlipped = puzzle.sideToMove == PieceColor.BLACK
        )
    }

    /** Called when the user taps a square on the board. */
    fun onSquareTapped(square: Square) {
        val state = _uiState.value
        if (state.isGameOver || state.pendingPromotion != null) return
        if (state.isBotThinking) return

        // In local modes both sides are "the player"; in vs-bot/online only allow moving own turn+color
        if (state.mode == GameMode.VS_BOT && state.gameState.sideToMove != state.playerColor) return

        val selected = state.selectedSquare
        if (selected != null) {
            val move = state.legalMovesForSelected.firstOrNull { it.to == square }
            if (move != null) {
                if (move.flag == MoveFlag.PROMOTION) {
                    _uiState.value = state.copy(
                        pendingPromotion = PendingPromotion(move.from, move.to, move.piece.color),
                        selectedSquare = null,
                        legalMovesForSelected = emptyList()
                    )
                } else {
                    applyMove(move)
                }
                return
            }
        }

        // Select / reselect
        val piece = state.gameState.board.pieceAt(square)
        if (piece != null && piece.color == state.gameState.sideToMove) {
            val moves = ChessEngine.legalMovesFrom(state.gameState, square)
            _uiState.value = state.copy(selectedSquare = square, legalMovesForSelected = moves)
        } else {
            _uiState.value = state.copy(selectedSquare = null, legalMovesForSelected = emptyList())
        }
    }

    fun confirmPromotion(type: PieceType) {
        val state = _uiState.value
        val pending = state.pendingPromotion ?: return
        val move = ChessEngine.legalMovesFrom(state.gameState, pending.from)
            .firstOrNull { it.to == pending.to && it.flag == MoveFlag.PROMOTION && it.promotionType == type }
            ?: return
        _uiState.value = state.copy(pendingPromotion = null)
        applyMove(move)
    }

    fun cancelPromotion() {
        _uiState.value = _uiState.value.copy(pendingPromotion = null)
    }

    private fun applyMove(move: Move) {
        val state = _uiState.value
        val before = state.gameState
        val after = ChessEngine.makeMove(before, move)
        val san = Notation.toSan(before, move, after)

        _uiState.value = state.copy(
            gameState = after,
            selectedSquare = null,
            legalMovesForSelected = emptyList(),
            sanMoves = state.sanMoves + san
        )

        playFeedback(move, after)

        if (state.timeControl.initialMinutes > 0) {
            addIncrement(before.sideToMove)
        }

        if (after.result.isGameOver) {
            onGameOver(after)
            return
        }

        maybeTriggerBotMove()
    }

    private fun playFeedback(move: Move, after: GameState) {
        when {
            after.result == GameResult.WHITE_WINS_CHECKMATE || after.result == GameResult.BLACK_WINS_CHECKMATE -> {
                soundManager.play(SoundEvent.CHECKMATE)
                haptics.trigger(HapticEvent.GAME_END)
            }
            ChessEngine.isInCheck(after, after.sideToMove) -> {
                soundManager.play(SoundEvent.CHECK)
                haptics.trigger(HapticEvent.CHECK)
            }
            move.capturedPiece != null -> {
                soundManager.play(SoundEvent.CAPTURE)
                haptics.trigger(HapticEvent.CAPTURE)
            }
            else -> {
                soundManager.play(SoundEvent.MOVE)
                haptics.trigger(HapticEvent.MOVE)
            }
        }
    }

    private fun maybeTriggerBotMove() {
        val state = _uiState.value
        if (state.mode != GameMode.VS_BOT) return
        if (state.isGameOver) return
        if (state.gameState.sideToMove == state.playerColor) return

        botJob?.cancel()
        _uiState.value = state.copy(isBotThinking = true)
        botJob = viewModelScope.launch {
            delay(250) // small pause so the bot doesn't feel instant/robotic
            val current = _uiState.value.gameState
            val bot = ChessBot(state.botDifficulty)
            val move = withContext(Dispatchers.Default) { bot.findBestMove(current) }
            if (move != null) {
                val before = current
                val after = ChessEngine.makeMove(before, move)
                val san = Notation.toSan(before, move, after)
                _uiState.value = _uiState.value.copy(
                    gameState = after,
                    isBotThinking = false,
                    sanMoves = _uiState.value.sanMoves + san
                )
                playFeedback(move, after)
                if (_uiState.value.timeControl.initialMinutes > 0) addIncrement(before.sideToMove)
                if (after.result.isGameOver) onGameOver(after)
            } else {
                _uiState.value = _uiState.value.copy(isBotThinking = false)
            }
        }
    }

    fun resign() {
        val state = _uiState.value
        if (state.isGameOver) return
        val result = if (state.gameState.sideToMove == PieceColor.WHITE) {
            GameResult.BLACK_WINS_RESIGN
        } else {
            GameResult.WHITE_WINS_RESIGN
        }
        val finished = state.gameState.copy(result = result)
        _uiState.value = state.copy(gameState = finished)
        onGameOver(finished)
    }

    fun undoLastMove() {
        val state = _uiState.value
        if (state.mode !in listOf(GameMode.LOCAL_TWO_PLAYER, GameMode.VS_BOT)) return
        if (state.gameState.moveHistory.isEmpty()) return

        // Rebuild from scratch by replaying all but the last move (and, vs bot,
        // the last two so it's the player's turn again).
        val stepsBack = if (state.mode == GameMode.VS_BOT) 2 else 1
        val movesToReplay = state.gameState.moveHistory.dropLast(stepsBack.coerceAtMost(state.gameState.moveHistory.size))
        var replayState = GameState.newGame()
        val sans = mutableListOf<String>()
        for (m in movesToReplay) {
            val before = replayState
            replayState = ChessEngine.makeMove(replayState, m)
            sans.add(Notation.toSan(before, m, replayState))
        }
        _uiState.value = state.copy(
            gameState = replayState,
            sanMoves = sans,
            selectedSquare = null,
            legalMovesForSelected = emptyList(),
            pendingPromotion = null
        )
    }

    private fun addIncrement(colorThatMoved: PieceColor) {
        val state = _uiState.value
        val inc = state.timeControl.incrementMillis
        _uiState.value = if (colorThatMoved == PieceColor.WHITE) {
            state.copy(whiteTimeMillis = state.whiteTimeMillis + inc)
        } else {
            state.copy(blackTimeMillis = state.blackTimeMillis + inc)
        }
    }

    private fun startClockIfNeeded() {
        val state = _uiState.value
        if (state.timeControl.initialMinutes <= 0) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(200)
                val current = _uiState.value
                if (current.isGameOver) break
                val toMove = current.gameState.sideToMove
                if (toMove == PieceColor.WHITE) {
                    val newTime = (current.whiteTimeMillis - 200).coerceAtLeast(0)
                    _uiState.value = current.copy(whiteTimeMillis = newTime)
                    if (newTime <= 0) {
                        val finished = current.gameState.copy(result = GameResult.BLACK_WINS_TIMEOUT)
                        _uiState.value = current.copy(gameState = finished, whiteTimeMillis = 0)
                        onGameOver(finished)
                        break
                    }
                } else {
                    val newTime = (current.blackTimeMillis - 200).coerceAtLeast(0)
                    _uiState.value = current.copy(blackTimeMillis = newTime)
                    if (newTime <= 0) {
                        val finished = current.gameState.copy(result = GameResult.WHITE_WINS_TIMEOUT)
                        _uiState.value = current.copy(gameState = finished, blackTimeMillis = 0)
                        onGameOver(finished)
                        break
                    }
                }
            }
        }
    }

    private fun onGameOver(finalState: GameState) {
        timerJob?.cancel()
        soundManager.play(SoundEvent.GAME_FINISH)
        haptics.trigger(HapticEvent.GAME_END)

        val state = _uiState.value
        _uiState.value = state.copy(gameOverMessage = resultMessage(finalState.result))

        if (state.mode == GameMode.PUZZLE) return // puzzles don't go into history

        viewModelScope.launch {
            val pgnText = Notation.buildPgnMoveText(state.sanMoves) + " " + Notation.resultTag(finalState.result)
            val opponentLabel = when (state.mode) {
                GameMode.VS_BOT -> "Bot (${state.botDifficulty.label})"
                GameMode.VS_FRIEND_ONLINE -> "Friend (Online)"
                GameMode.LOCAL_TWO_PLAYER -> "Local 2P"
                GameMode.PUZZLE -> "Puzzle"
            }
            historyRepository.addEntry(
                GameHistoryEntry(
                    id = UUID.randomUUID().toString(),
                    dateEpochMillis = System.currentTimeMillis(),
                    mode = state.mode,
                    result = finalState.result,
                    playerColor = state.playerColor,
                    opponentLabel = opponentLabel,
                    moveCount = state.sanMoves.size,
                    pgn = pgnText
                )
            )
        }
    }

    private fun resultMessage(result: GameResult): String = when (result) {
        GameResult.WHITE_WINS_CHECKMATE -> "Checkmate \u2014 White wins"
        GameResult.BLACK_WINS_CHECKMATE -> "Checkmate \u2014 Black wins"
        GameResult.WHITE_WINS_RESIGN -> "Black resigned \u2014 White wins"
        GameResult.BLACK_WINS_RESIGN -> "White resigned \u2014 Black wins"
        GameResult.WHITE_WINS_TIMEOUT -> "Black ran out of time \u2014 White wins"
        GameResult.BLACK_WINS_TIMEOUT -> "White ran out of time \u2014 Black wins"
        GameResult.DRAW_STALEMATE -> "Draw \u2014 Stalemate"
        GameResult.DRAW_THREEFOLD_REPETITION -> "Draw \u2014 Threefold repetition"
        GameResult.DRAW_FIFTY_MOVE_RULE -> "Draw \u2014 Fifty-move rule"
        GameResult.DRAW_INSUFFICIENT_MATERIAL -> "Draw \u2014 Insufficient material"
        GameResult.DRAW_AGREEMENT -> "Draw agreed"
        GameResult.IN_PROGRESS -> ""
    }

    fun copyPgn(): String {
        val state = _uiState.value
        return Notation.buildPgnMoveText(state.sanMoves) + " " + Notation.resultTag(state.gameState.result)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        botJob?.cancel()
    }
}
