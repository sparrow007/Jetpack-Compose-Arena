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
//
//    Spacer(
//        modifier = Modifier
//            .drawWithCache {
//                val path = Path()
//                path.moveTo(0f, 0f)
//                path.lineTo(size.width / 2f, size.height / 2f)
//                // path.lineTo(size.width, 0f)
//                // path.close()
//                onDrawBehind {
//                    drawPath(path, Color.Magenta, style = Stroke(width = 10f))
//                }
//            }
//            .fillMaxSize()
//    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(size.width / 2f, size.height / 2f)
        path.lineTo(size.width.toFloat(), 0f)


        drawPath(path, Color.Green, style = Stroke(width = 10f))

    }

}