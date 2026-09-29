package com.mastermind.viewModel

import androidx.lifecycle.ViewModel
import com.mastermind.data.GameRepository
import com.mastermind.models.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    private val _numColors = MutableStateFlow(6)
    val numColors: StateFlow<Int> = _numColors

    private val _codeLength = MutableStateFlow(4)
    val codeLength: StateFlow<Int> = _codeLength

    private val _allowDuplicates = MutableStateFlow(true)
    val allowDuplicates: StateFlow<Boolean> = _allowDuplicates

    private val _hasCurrentGame = MutableStateFlow(false)
    val hasCurrentGame: StateFlow<Boolean> = _hasCurrentGame

    private val _currentGame = MutableStateFlow<GameState?>(null)

    init {
        refreshCurrentGame()
    }

    fun setNumColors(value: Int) {
        _numColors.value = value
    }

    fun setCodeLength(value: Int) {
        _codeLength.value = value
    }

    fun setAllowDuplicates(value: Boolean) {
        _allowDuplicates.value = value
    }

    fun refreshCurrentGame() {
        viewModelScope.launch(Dispatchers.IO) {
            val game = repository.getCurrentGame()

            _currentGame.value = game
            _hasCurrentGame.value = game != null && !game.isGameOver
        }
    }

    fun getCurrentGame(): GameState? {
        return _currentGame.value
    }
}