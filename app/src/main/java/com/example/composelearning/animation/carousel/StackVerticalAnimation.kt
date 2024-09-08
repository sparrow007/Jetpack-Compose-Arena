package com.example.composelearning.animation.carousel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun StackCard(cards: List<Color>) {

    Box(modifier = Modifier.fillMaxSize()) {
        cards.forEachIndexed { index, color ->

        }
    }
}

@Composable
fun ShowVerticalCardStack() {
    val list = listOf<Color>(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFD54F),
        Color(0xFF9575CD),
        Color(0xFF4DB6AC)
    )

}