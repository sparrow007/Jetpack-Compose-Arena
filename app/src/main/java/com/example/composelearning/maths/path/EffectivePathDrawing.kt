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
        path.moveTo(0f, 150f)
        path.lineTo(0f, 150f)
        path.quadraticBezierTo(size.width / 2, 150f, size.width, 150f)
        this.drawPath(path, Color.Blue, style = Stroke(5f))

    }

}