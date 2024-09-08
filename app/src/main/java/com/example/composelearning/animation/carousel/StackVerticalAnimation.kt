package com.example.composelearning.animation.carousel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun StackCard(colorList: List<Color>, size: Int) {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        colorList.forEachIndexed { index, color ->
            val density = LocalDensity.current
            val yOffset = with(density) {
                (size - (size - index) * 30).toDp()
            }
            //you can use linear interpolation to create scalable value range for 0 to 1
            val scale = 1f - (size - index) * 0.02f
            ShowCards(modifier = Modifier
                .size(180.dp, 230.dp)
                .offset(y = yOffset)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }, color = color)
        }
    }
}

@Composable
fun ShowCards(modifier: Modifier, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
    ) {}
}

@Composable
@Preview(showSystemUi = true)
fun ShowVerticalCardStack() {
    val list = listOf<Color>(
        Color(0xFF64B5F6),
        Color(0xFFFFD54F),
        Color(0xFF9575CD),
        Color(0xFF4DB6AC)
    )
    StackCard(list, list.size)
}