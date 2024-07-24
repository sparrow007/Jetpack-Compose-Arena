package com.example.composelearning.maths.path.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun getGraphPath(graphLines: Int = 40, windowSize: Pair<Dp, Dp>): Path {
    val path = Path()

    val (width, height) = windowSize
    val widthInPixel = LocalDensity.current.run { width.toPx() }
    val heightInPixel = LocalDensity.current.run { height.toPx() }

    //Y axis graph lines
    val yAxisLines = heightInPixel / graphLines
    for (i in 0..graphLines) {
        path.moveTo(0f, i * yAxisLines)
        path.lineTo(widthInPixel, i * yAxisLines)
    }

    //x axis graph lines
    val xAxisLines = widthInPixel / graphLines
    for (i in 0..graphLines) {
        path.moveTo(i * xAxisLines, 0f)
        path.lineTo(i * xAxisLines, heightInPixel)
    }

    return path
}