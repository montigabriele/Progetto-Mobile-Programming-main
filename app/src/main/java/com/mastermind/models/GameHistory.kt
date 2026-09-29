package com.mastermind.models

import java.io.Serializable
import java.util.Date

data class GameHistory(
    val id: Int,
    val settings: GameSettings,
    val isWon: Boolean,
    val score: Int,
    val attempts: Int,
    val date: Date
) : Serializable