package com.mastermind.models

import androidx.compose.ui.graphics.Color
import com.mastermind.ui.theme.*

enum class ColorPeg(val value: Int) {
    RED(0), GREEN(1), BLUE(2), YELLOW(3),
    ORANGE(4), PURPLE(5), PINK(6), CYAN(7),
    BROWN(8), BLACK(9), EMPTY(-1);

    val color: Color
        get() = when (this) {
            RED -> PegRed
            GREEN -> PegGreen
            BLUE -> PegBlue
            YELLOW -> PegYellow
            ORANGE -> PegOrange
            PURPLE -> PegPurple
            PINK -> PegPink
            CYAN -> PegCyan
            BROWN -> PegBrown
            BLACK -> PegGray
            EMPTY -> Color.Transparent
        }
}