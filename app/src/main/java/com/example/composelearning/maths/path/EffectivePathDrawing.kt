package com.example.composelearning.maths.path

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview

@Preview(showSystemUi = true)
@Composable
fun CreateNormalPath() {

//    var animState by remember {
//        mutableStateOf(150f)
//    }

    var controlPoint by remember {
        mutableStateOf(PointF(0f, 0f))
    }

    var controlPoint2 by remember {
        mutableStateOf(PointF(0f, 0f))
    }

//    val scale = animateFloatAsState(
//        targetValue =
//        if (animState == 150f) 80f else 150f,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioHighBouncy,
//            stiffness = Spring.StiffnessLow
//        ), label = ""
//    )
    Column(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    controlPoint = PointF(
                        controlPoint.x + dragAmount.x,
                        controlPoint.y + dragAmount.y
                    )
                }
            }
        ) {

            val path = Path()
            path.reset()
            if (controlPoint.x == 0f && controlPoint.y == 0f) {
                controlPoint = PointF(size.width / 2f - 250f, size.height / 2f - 150f)
            }
            if (controlPoint2.x == 0f && controlPoint2.y == 0f) {
                controlPoint2 = PointF(size.width / 2f + 250f, size.height / 2f - 250f)
            }

            val cp = PointF(size.width / 2f, size.height / 2f - 150f)
            //controlPoint = cp
            val point2 = PointF(size.width / 2f + 150f, size.height / 2f)
            path.moveTo(size.width / 2f - 150f, size.height / 2f)

            path.cubicTo(
                controlPoint.x, controlPoint.y,
                controlPoint2.x, controlPoint2.y,
                point2.x, point2.y
            )


            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 5f)
            )

        }

    }

    fun Modifier.swipeToHandle() {
        pointerInput(Unit) {
            this.detectDragGestures { change, dragAmount ->

            }
        }
    }

}