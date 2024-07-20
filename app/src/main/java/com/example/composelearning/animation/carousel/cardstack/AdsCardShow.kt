package com.example.composelearning.animation.carousel.cardstack

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun AdsCardShow() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        val elements = 4
        for (i in 0 until elements) {

            CardsView(
                modifier = Modifier
                    .offset {
                        IntOffset(((elements - (i + 1)) * -12), 0)
                    }
                    .graphicsLayer {
                        rotationZ = ((elements - (i + 1)) * -10).toFloat()
                    }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsView(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(170.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
        onClick = { /*TODO*/ }) {

    }
}
