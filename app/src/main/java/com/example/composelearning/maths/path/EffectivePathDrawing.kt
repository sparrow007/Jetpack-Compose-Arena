package com.example.composelearning.maths.path

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun CreateNormalPath() {

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()

        val wave = WaveLayer(
            waveCenterY = 200f,
            waveHorRadius = 100f,
            waveVertRadius = 100f,
            sideWidth = 100f,
            swipeDirection = 1
        )
        wave.updatePath(this.size.width, this.size.height)
        path.addPath(wave.path)

        //Create the path for the cubic and quad to test out the different parts



        drawPath(wave.path, Color.Green, style = Stroke(width = 10f))

    }

}