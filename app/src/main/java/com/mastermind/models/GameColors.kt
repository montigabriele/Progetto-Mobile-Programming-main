package com.mastermind.models

object GameColors {
    fun getColorPegs(count: Int): List<ColorPeg> {
        return ColorPeg.entries.filter { it != ColorPeg.EMPTY }.take(count)
    }
}