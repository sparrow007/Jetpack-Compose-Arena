package com.example.composelearning.customlayout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@Composable
fun SnapCarouselLayout(
    numberOfItems: Int,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int) -> Unit
) {
    require(numberOfItems > 0) { "Number of items should be greater than 0" }

    Layout(modifier = modifier, content = {
        repeat(numberOfItems) { index ->
            content(index)
        }
    }) { measurables, constraints ->
        val itemConstraints = Constraints.fixed(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        )

        val placeables = measurables.map { it.measure(itemConstraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val centerX = constraints.maxWidth / 2
            val centerY = constraints.maxHeight / 2

            placeables.forEachIndexed { index, placeable ->
                val xPosition = when (index) {
                    0 -> 0
                    numberOfItems - 1 -> constraints.maxWidth - placeable.width
                    else -> centerX - placeable.width / 2
                }
                val yPosition = centerY - placeable.height / 2

                placeable.placeRelative(x = xPosition, y = yPosition)
            }
        }
    }
}

@Preview
@Composable
fun PreviewSnapCarouselLayout() {
    SnapCarouselLayout(numberOfItems = 3) { index ->
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 100.dp)
                .background(
                    color = when (index) {
                        0 -> Color.Red
                        1 -> Color.Green
                        else -> Color.Blue
                    }
                )
                .fillMaxSize()
        )
    }
}