package com.example.composelearning.animation.carousel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


@Composable
fun StackCard(colorList: List<Color>, size: Int) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        colorList.forEachIndexed { index, color ->
            val density = LocalDensity.current
            var yOffset = with(density) {
                (size - (size - index) * 140).toDp()
            }

            //you can use linear interpolation to create scalable value range for 0 to 1
            val scale = 1f - (size - index) * 0.05f
            ShowCards(modifier = Modifier
                .size(180.dp, 230.dp)
                .offset(y = yOffset)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationX = -(index * 3f)
                    spotShadowColor = Color.White
                    ambientShadowColor = Color.Gray
                    cameraDistance = 5f
                }
                .zIndex(index.toFloat()) // Add this line
                , color = color, index = index
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ShowCards(modifier: Modifier, color: Color, index: Int) {
    val elevation = with(LocalDensity.current) {
        (index * 100).toDp()
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
            .shadow(elevation = elevation, shape = RoundedCornerShape(10.dp)), // Add this line ,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation,
            draggedElevation = 20.dp,

            )
    ) {}
}

@Composable
fun ShowCardsShadow(modifier: Modifier, color: Color, index: Int) {
    val elevation = with(LocalDensity.current) {
        (index * 20).toDp()
    }
    Box(
        modifier = modifier
            .offset(y = -elevation)
            .fillMaxWidth()
            .height(elevation)
            .background(color = Color.Black.copy(alpha = 0.2f))
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp, // Remove the default elevation
            draggedElevation = 20.dp,
        )
    ) {}
}

@Composable
@Preview(showSystemUi = true)

fun ShowVerticalCardStack() {
    val list = listOf<Color>(
        Color(0xFF99CDF7),
        Color(0xFFFAE294),
        Color(0xFFBA98F7),
        Color(0xFF83F7EC)
    )
    StackCard(list, list.size)
}