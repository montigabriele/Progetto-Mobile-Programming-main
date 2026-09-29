package com.mastermind.models

data class Attempt(
    val guess: List<ColorPeg>,
    val correctPosition: Int = 0,
    val correctColor: Int = 0
)