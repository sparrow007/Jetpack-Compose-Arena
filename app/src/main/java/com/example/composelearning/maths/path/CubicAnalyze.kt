package com.example.composelearning.maths.path

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composelearning.maths.path.graph.getGraphPath

@Composable
@Preview
fun CubicAnalyze() {
    val path = getGraphPath(windowSize = getScreenDimensions())
    Canvas(modifier = Modifier.fillMaxSize()) {
        //Let's create a graph in the canvas
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        val stroke = Stroke(width = 1f, pathEffect = pathEffect)
        this.drawPath(path, color = Color.White, style = stroke)

    }
}

@Composable
fun getScreenDimensions(): Pair<Dp, Dp> {
    val configuration = LocalConfiguration.current
    return Pair(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
}


