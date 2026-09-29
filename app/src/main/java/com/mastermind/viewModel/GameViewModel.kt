package com.mastermind.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mastermind.data.GameRepository
import com.mastermind.models.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.random.Random

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private var nextGameId = Random.nextInt(100000, 999999)

    private val _gameSettings = MutableStateFlow(GameSettings(numColors = 6, codeLength = 4, allowDuplicates = true, maxAttempts = 10))

    private val _secretCode = MutableStateFlow(
        GameLogic.generateSecretCode(_gameSettings.value, GameColors.getColorPegs(_gameSettings.value.numColors))
    )

    private val _gameState = MutableStateFlow(
        GameState(
            id = nextGameId,
            settings = _gameSettings.value,
            secretCode = _secretCode.value
        )
    )
    val gameState: StateFlow<GameState> = _gameState

    private var compatibleCombinationsJob: Job? = null

    private val _currentAttempt = MutableStateFlow(List(_gameSettings.value.codeLength) { ColorPeg.EMPTY })
    val currentAttempt: StateFlow<List<ColorPeg>> = _currentAttempt

    private val _selectedColorIndex = MutableStateFlow(0)
    val selectedColorIndex: StateFlow<Int> = _selectedColorIndex

    private val _gameTime = MutableStateFlow(0)
    val gameTime: StateFlow<Int> = _gameTime

    private val _savedGames = MutableStateFlow<List<GameState>>(emptyList())
    val savedGames: StateFlow<List<GameState>> = _savedGames

    private val _isFromHistory = MutableStateFlow(false)
    val isFromHistory: StateFlow<Boolean> = _isFromHistory

    private val _isGameReady = MutableStateFlow(false)
    val isGameReady: StateFlow<Boolean> = _isGameReady

    private val _compatibleCombinations = MutableStateFlow(0)
    val compatibleCombinations: StateFlow<Int> = _compatibleCombinations

    init {
        loadSavedGames()
    }

    fun setSelectedColor(index: Int) {
        _selectedColorIndex.value = index
        val firstEmptyIndex = _currentAttempt.value.indexOfFirst { it == ColorPeg.EMPTY }
        if (firstEmptyIndex != -1) {
            val newAttempt = _currentAttempt.value.toMutableList()
            newAttempt[firstEmptyIndex] = GameColors.getColorPegs(_gameSettings.value.numColors)[index]
            _currentAttempt.value = newAttempt
        }
    }

    fun resetAttempt() {
        _currentAttempt.value = List(_gameSettings.value.codeLength) { ColorPeg.EMPTY }
    }

    fun setPegEmpty(index: Int) {
        val newAttempt = _currentAttempt.value.toMutableList()
        newAttempt[index] = ColorPeg.EMPTY
        _currentAttempt.value = newAttempt
    }

    private fun updateCompatibleCombinations() {
        val state = _gameState.value

        compatibleCombinationsJob?.cancel()

        if (state.isGameOver) {
            return
        }

        if (state.attempts.isEmpty()) {
            _compatibleCombinations.value =
                GameLogic.getTotalCombinations(state.settings).toInt()
            return
        }

        compatibleCombinationsJob = viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                GameLogic.countCompatibleCodes(
                    state.settings,
                    state.attempts
                )
            }

            val currentState = _gameState.value

            if (
                currentState.id == state.id &&
                currentState.attempts.size == state.attempts.size &&
                !currentState.isGameOver
            ) {
                _compatibleCombinations.value = result
            }
        }
    }

    fun confirmAttempt() {
        if (_currentAttempt.value.none { it == ColorPeg.EMPTY }) {

            val updatedState = GameLogic.applyGuess(
                _gameState.value,
                _currentAttempt.value
            ).copy(
                gameTime = _gameTime.value
            )

            _gameState.value = updatedState

            _currentAttempt.value =
                List(updatedState.settings.codeLength) {
                    ColorPeg.EMPTY
                }

            _selectedColorIndex.value = 0

            if (updatedState.isGameOver) {

                compatibleCombinationsJob?.cancel()
                
                viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    withContext(Dispatchers.IO + NonCancellable) {
                        repository.clearCurrentGame(updatedState.id)
                        repository.saveGame(updatedState)

                        repository.addHistory(
                            GameHistory(
                                id = updatedState.id,
                                settings = updatedState.settings,
                                isWon = updatedState.isWon,
                                score = updatedState.getScore(updatedState.gameTime),
                                attempts = updatedState.getUsedAttempts(),
                                date = Date()
                            )
                        )

                        _savedGames.value = repository.getSavedGames()
                    }
                }

            } else {
                updateCompatibleCombinations()
            }
        }
    }

    fun newGame(settings: GameSettings? = null) {
        settings?.let { _gameSettings.value = it }
        val s = _gameSettings.value

        nextGameId = Random.nextInt(100000, 999999)

        val newSecretCode = GameLogic.generateSecretCode(
            s,
            GameColors.getColorPegs(s.numColors)
        )

        _secretCode.value = newSecretCode

        _gameState.value = GameState(
            id = nextGameId,
            settings = s,
            secretCode = newSecretCode,
            gameTime = 0
        )

        resetAttempt()
        _gameTime.value = 0

        _isFromHistory.value = false
        _isGameReady.value = true

        updateCompatibleCombinations()
    }

    fun initializeNewGameIfNeeded(settings: GameSettings) {
        if (!_isGameReady.value) {
            newGame(settings)
        }
    }

    fun incrementTime() {
        _gameTime.value += 1
    }

    fun saveCurrentGame(onSaved: () -> Unit = {}) {

        val stateToSave = _gameState.value.copy(
            gameTime = _gameTime.value
        )

        viewModelScope.launch {

            val games = withContext(Dispatchers.IO) {
                repository.saveCurrentGame(stateToSave)
                repository.getSavedGames()
            }

            _savedGames.value = games

            onSaved()
        }
    }

    fun saveGame() {
        val currentState =
            _gameState.value.copy(gameTime = _gameTime.value)

        viewModelScope.launch(Dispatchers.IO) {
            if (currentState.isGameOver) {
                repository.clearCurrentGame(currentState.id)
            } else {
                repository.saveCurrentGame(currentState)
            }

            _savedGames.value = repository.getSavedGames()
        }
    }

    fun loadSavedGames() {
        viewModelScope.launch(Dispatchers.IO) {
            _savedGames.value = repository.getSavedGames()
        }
    }

    fun loadGame(
        gameState: GameState,
        fromHistory: Boolean = true
    ) {
        compatibleCombinationsJob?.cancel()

        _gameSettings.value = gameState.settings
        _secretCode.value = gameState.secretCode
        _gameState.value = gameState
        _isFromHistory.value = fromHistory

        resetAttempt()

        _gameTime.value = gameState.gameTime
        _isGameReady.value = true

        if (!gameState.isGameOver) {
            updateCompatibleCombinations()
        } else {
            _compatibleCombinations.value = 0
        }
    }

    suspend fun loadGameById(
        gameId: Int,
        fromHistory: Boolean
    ): Boolean {

        if (
            _isGameReady.value &&
            _gameState.value.id == gameId
        ) {
            _isFromHistory.value = fromHistory
            return true
        }

        val game = withContext(Dispatchers.IO) {
            repository.getCurrentGame()
                ?.takeIf { it.id == gameId }
                ?: repository.getSavedGames()
                    .find { it.id == gameId }
        }

        return if (game != null) {
            loadGame(game, fromHistory)
            true
        } else {
            false
        }
    }

    fun deleteSavedGame(gameId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavedGame(gameId)
            _savedGames.value = repository.getSavedGames()
        }
    }

    fun clearCurrentGame() {
        val gameId = _gameState.value.id

        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCurrentGame(gameId)
        }
    }
}