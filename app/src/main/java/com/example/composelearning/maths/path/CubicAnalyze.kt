package com.example.composelearning.maths.path

import android.graphics.Paint
import android.graphics.Picture
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun CubicAnalyze() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val picture = Picture()
        val paint = Paint()
        val canvas = picture.beginRecording(size.width.toInt(), size.height.toInt())
        canvas.drawCircle(100f, 100f, 50f, android.graphics.Paint())
        this.drawIntoCanvas {
            it.nativeCanvas.drawPicture(picture)
        }
    }
}
