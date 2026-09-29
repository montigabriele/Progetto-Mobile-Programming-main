package com.mastermind.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mastermind.R

@Composable
fun MastermindBackground(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.04f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06182A))
    ) {

        Image(
            painter = painterResource(
                id = R.drawable.mastermind_background
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center,
            contentScale = ContentScale.FillBounds
        )

        if (scrimColor.alpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor)
            )
        }

        content()
    }
}