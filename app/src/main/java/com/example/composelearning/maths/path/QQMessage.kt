package com.example.composelearning.maths.path

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

@Composable
@Preview
fun QQMessageEffect() {

    val startPoint = remember {
        PointF(100f, 100f)
    }

    val endPoint = remember {
        mutableStateOf(PointF(300f, 300f))
    }

    val mRadius = 50f

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                endPoint.value = PointF(
                    endPoint.value.x + dragAmount.x,
                    endPoint.value.y + dragAmount.y
                )
            }
        }
    ) {
        val path = calculateQQMessagePath(startPoint, endPoint.value, mRadius)

        drawCircle(
            color = Color.Red,
            center = Offset(startPoint.x, startPoint.y),
            radius = mRadius
        )
        drawCircle(
            color = Color.Red,
            center = Offset(endPoint.value.x, endPoint.value.y),
            radius = mRadius
        )
        drawPath(path, Color.Red)
    }
}

fun calculateQQMessagePath(startPoint: PointF, endPoint: PointF, mRadius: Float): Path {
    val path = Path()

    val endX = endPoint.x
    val endY = endPoint.y
    val startX = startPoint.x
    val startY = startPoint.y

    val dx = endX - startX
    val dy = endY - startY
    val angle = atan(dy / dx.toDouble())

    val offsetX = (mRadius * sin(angle)).toFloat()
    val offsetY = (mRadius * cos(angle).toFloat())

    val x1 = startX + offsetX
    val y1 = startY - offsetY

    val x2 = endX + offsetX
    val y2 = endY - offsetY

    val x3 = endX - offsetX
    val y3 = endY + offsetY

    val x4 = startX - offsetX
    val y4 = startY + offsetY

    val anchorPointX = (startX + endX) / 2
    val anchorPointY = (startY + endY) / 2

    path.reset()
    path.moveTo(x1, y1)
    path.quadraticBezierTo(anchorPointX, anchorPointY, x2, y2)
    path.lineTo(x3, y3)
    path.quadraticBezierTo(anchorPointX, anchorPointY, x4, y4)
    //path.close()


    return path
}