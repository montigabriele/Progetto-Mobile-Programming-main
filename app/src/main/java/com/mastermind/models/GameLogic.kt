package com.mastermind.models

object GameLogic {

    fun generateSecretCode(settings: GameSettings, palette: List<ColorPeg>): List<ColorPeg> {
        val secretCode = mutableListOf<ColorPeg>()

        if (settings.allowDuplicates) {
            repeat(settings.codeLength) {
                secretCode.add(palette.random())
            }
        } else {
            val shuffledColors = palette.shuffled()
            secretCode.addAll(shuffledColors.take(settings.codeLength))
        }
        return secretCode
    }

    private fun evaluateGuess(secret: List<ColorPeg>, guess: List<ColorPeg>): Attempt {
        var correctPosition = 0
        var correctColor = 0

        val secretCopy: MutableList<ColorPeg?> = secret.map { it }.toMutableList()
        val guessCopy: MutableList<ColorPeg?> = guess.map { it }.toMutableList()

        for (i in guess.indices) {
            if (guess[i] == secret[i]) {
                correctPosition++
                secretCopy[i] = null
                guessCopy[i] = null
            }
        }

        for (j in guess.indices) {
            val g = guessCopy[j]
            if (g != null) {
                val indexInSecret = secretCopy.indexOf(g)
                if (indexInSecret != -1) {
                    correctColor++
                    secretCopy[indexInSecret] = null
                    guessCopy[j] = null
                }
            }
        }

        return Attempt(guess, correctPosition, correctColor)
    }

    fun applyGuess(state: GameState, guess: List<ColorPeg>): GameState {
        val attempt = evaluateGuess(state.secretCode, guess)
        val updateAttempts = state.attempts + attempt

        val isWon = attempt.correctPosition == state.settings.codeLength
        val isGameOver = updateAttempts.size >= state.settings.maxAttempts || isWon

        return state.copy(
            attempts = updateAttempts,
            isWon = isWon,
            isGameOver = isGameOver
        )
    }


    fun getTotalCombinations(settings: GameSettings): Long {
        val numColors = settings.numColors.toLong()
        val codeLength = settings.codeLength

        return if (settings.allowDuplicates) {
            var result = 1L
            repeat(codeLength) {
                result *= numColors
            }
            result
        } else {
            if (codeLength > numColors) {
                0L
            } else {
                var result = 1L
                for (i in 0 until codeLength) {
                    result *= (numColors - i)
                }
                result
            }
        }
    }

    fun countCompatibleCodes(settings: GameSettings, attempts: List<Attempt>): Int {
        if (attempts.isEmpty()) {
            return getTotalCombinations(settings).toInt()
        }

        val availableColors = GameColors.getColorPegs(settings.numColors)
        var compatibleCount = 0

        if (settings.allowDuplicates) {
            generateWithDuplicates(availableColors, settings.codeLength) { combination ->
                if (isCompatibleWithAllAttempts(combination, attempts)) {
                    compatibleCount++
                }
            }
        } else {
            generateWithoutDuplicates(availableColors, settings.codeLength) { combination ->
                if (isCompatibleWithAllAttempts(combination, attempts)) {
                    compatibleCount++
                }
            }
        }

        return compatibleCount
    }

    private fun generateWithDuplicates(
        colors: List<ColorPeg>,
        length: Int,
        callback: (List<ColorPeg>) -> Unit
    ) {
        val current = MutableList(length) { colors[0] }
        val indices = IntArray(length)

        do {
            for (i in 0 until length) {
                current[i] = colors[indices[i]]
            }
            callback(current.toList())

            var carry = 1
            for (i in length - 1 downTo 0) {
                indices[i] += carry
                if (indices[i] < colors.size) {
                    carry = 0
                    break
                } else {
                    indices[i] = 0
                }
            }
        } while (carry == 0)
    }

    private fun generateWithoutDuplicates(
        colors: List<ColorPeg>,
        length: Int,
        callback: (List<ColorPeg>) -> Unit
    ) {
        if (length > colors.size) return

        val current = mutableListOf<ColorPeg>()
        val used = BooleanArray(colors.size) { false }

        fun backtrack(position: Int) {
            if (position == length) {
                callback(current.toList())
                return
            }

            for (i in colors.indices) {
                if (!used[i]) {
                    used[i] = true
                    current.add(colors[i])
                    backtrack(position + 1)
                    current.removeAt(current.size - 1)
                    used[i] = false
                }
            }
        }

        backtrack(0)
    }

    private fun isCompatibleWithAllAttempts(combination: List<ColorPeg>, attempts: List<Attempt>): Boolean {
        for (attempt in attempts) {
            val evaluation = evaluateGuess(combination, attempt.guess)
            if (evaluation.correctPosition != attempt.correctPosition ||
                evaluation.correctColor != attempt.correctColor) {
                return false
            }
        }
        return true
    }
}