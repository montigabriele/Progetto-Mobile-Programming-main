package com.mastermind.models

import java.io.Serializable
import java.util.Date

data class GameState(
    val id: Int,
    val settings: GameSettings,
    val secretCode: List<ColorPeg>,
    val attempts: List<Attempt> = listOf(),
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val compatibleMovesCount: Long = -1L,
    val date: Date = Date(),
    val gameTime: Int = 0
) : Serializable {
    fun getUsedAttempts(): Int = attempts.size

    fun getScore(gameTime: Int): Int {
        if (!isWon) return 0

        val attemptBonus = maxOf(0, settings.maxAttempts - attempts.size) * 100

        val timeBonus = when {
            gameTime <= 60 -> 200
            gameTime <= 180 -> 100
            gameTime <= 300 -> 50
            else -> 0
        }

        val difficultyMultiplier = 1 + (settings.codeLength + settings.numColors - 6) * 0.1f

        return ((attemptBonus + timeBonus) * difficultyMultiplier).toInt()
    }

}