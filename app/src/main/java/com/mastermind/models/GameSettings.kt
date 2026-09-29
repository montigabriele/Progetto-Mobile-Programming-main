package com.mastermind.models

data class GameSettings(
    val numColors: Int,
    val codeLength: Int,
    val allowDuplicates: Boolean,
    val maxAttempts: Int = 10
)
