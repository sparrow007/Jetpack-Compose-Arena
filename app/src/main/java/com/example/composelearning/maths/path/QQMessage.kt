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
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
@Preview
fun QQMessageEffect() {

    val startPoint = remember {
        PointF(0f, 0f)
    }

    val endPoint = remember {
        mutableStateOf(PointF(0f, 0f))
    }

    var mRadius = 90f

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures { _, dragAmount ->
                endPoint.value = PointF(
                    endPoint.value.x + dragAmount.x,
                    endPoint.value.y + dragAmount.y
                )
            }
        }
    ) {

        if (startPoint.x == 0f && startPoint.y == 0f) {
            startPoint.x = size.width / 2f
            startPoint.y = size.height / 2f
        }

        if (endPoint.value.x == 0f && endPoint.value.y == 0f) {
            endPoint.value.x = size.width / 2f + 200f
            endPoint.value.y = size.height / 2f + 200f
        }

        val circleDistance = sqrt(
            (startPoint.x - endPoint.value.x).toDouble().pow(2) +
                    (startPoint.y - endPoint.value.y).toDouble().pow(2)
        )

        val radius = (50f - circleDistance / 20).toFloat()

        val path = calculateQQMessagePath(startPoint, endPoint.value, radius)

        drawCircle(
            color = Color.Red,
            center = Offset(startPoint.x, startPoint.y),
            radius = radius
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