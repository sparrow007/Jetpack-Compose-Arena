package com.example.composelearning.maths.path

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Preview
@Composable
fun MetaballEffect() {
    var targetAngle by remember { mutableStateOf(0f) }
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 1000)
    )

    var toggle by remember { mutableStateOf(false) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = if (toggle) 300f else 200f,
        animationSpec = tween(durationMillis = 1000)
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (toggle) 400f else 300f,
        animationSpec = tween(durationMillis = 1000)
    )

    var center1 by remember { mutableStateOf(Offset(animatedOffsetX, 200f)) }
    var center2 by remember { mutableStateOf(Offset(400f, animatedOffsetY)) }
    var center3 by remember { mutableStateOf(Offset(animatedOffsetX, 400f)) }
    var center4 by remember { mutableStateOf(Offset(400f, animatedOffsetY)) }


    LaunchedEffect(Unit) {
        while (true) {
            toggle = !toggle
            delay(2000)
        }
    }


    Canvas(modifier = Modifier
        .fillMaxSize()
        .clickable {
            targetAngle += 45f
        }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                val dragOffset = Offset(dragAmount.x, dragAmount.y)
                when {
                    center1.distanceTo(change.position) < 50.dp.toPx() -> {
                        center1 = center1 + dragOffset
                    }

                    center2.distanceTo(change.position) < 50.dp.toPx() -> {
                        center2 = center2 + dragOffset
                    }

                    center3.distanceTo(change.position) < 50.dp.toPx() -> {
                        center3 = center3 + dragOffset
                    }

                    center4.distanceTo(change.position) < 50.dp.toPx() -> {
                        center4 = center4 + dragOffset
                    }
                }
            }
        }
    ) {
        rotate(degrees = animatedAngle, pivot = center) {
            val radius = 50.dp.toPx()
            drawMetaball(center1, center2, radius, this)
            drawMetaball(center1, center3, radius, this)
            drawMetaball(center1, center4, radius, this)
            drawMetaball(center2, center3, radius, this)
            drawMetaball(center2, center4, radius, this)
            drawMetaball(center3, center4, radius, this)
        }

    }
}

fun drawMetaball(center1: Offset, center2: Offset, radius: Float, drawScope: DrawScope) {
    val distance = center1.distanceTo(center2)

    if (distance <= radius * 3 && distance > 0f) {
        val u = 2.0 + (distance / (radius * 3)) * 2.0
        val v = min(1.0, sqrt(radius * 3 / distance) * 0.5)

        val angleBetweenCenters =
            atan2((center2.y - center1.y).toDouble(), (center2.x - center1.x).toDouble())
        val angleOffset = acos(((radius - radius * v) / distance).coerceIn(-1.0, 1.0))

        val angle1 = angleBetweenCenters + angleOffset
        val angle2 = angleBetweenCenters - angleOffset
        val angle3 = angleBetweenCenters + PI - angleOffset
        val angle4 = angleBetweenCenters + PI + angleOffset

        val p1 = Offset(
            (center1.x + radius * cos(angle1)).toFloat(),
            (center1.y + radius * sin(angle1)).toFloat()
        )
        val p2 = Offset(
            (center1.x + radius * cos(angle2)).toFloat(),
            (center1.y + radius * sin(angle2)).toFloat()
        )
        val p3 = Offset(
            (center2.x + radius * cos(angle3)).toFloat(),
            (center2.y + radius * sin(angle3)).toFloat()
        )
        val p4 = Offset(
            (center2.x + radius * cos(angle4)).toFloat(),
            (center2.y + radius * sin(angle4)).toFloat()
        )

        val controlPoint = Offset(
            (0.5 * (center1.x + center2.x)).toFloat(),
            (0.5 * (center1.y + center2.y)).toFloat()
        )

        val path = Path().apply {
            moveTo(p1.x, p1.y)
            cubicTo(
                ((p1.x + u * controlPoint.x) / (1 + u)).toFloat(),
                ((p1.y + u * controlPoint.y) / (1 + u)).toFloat(),
                ((p3.x + u * controlPoint.x) / (1 + u)).toFloat(),
                ((p3.y + u * controlPoint.y) / (1 + u)).toFloat(),
                p3.x, p3.y
            )
            lineTo(p4.x, p4.y)
            cubicTo(
                ((p4.x + u * controlPoint.x) / (1 + u)).toFloat(),
                ((p4.y + u * controlPoint.y) / (1 + u)).toFloat(),
                ((p2.x + u * controlPoint.x) / (1 + u)).toFloat(),
                ((p2.y + u * controlPoint.y) / (1 + u)).toFloat(),
                p2.x, p2.y
            )
            close()
        }

        drawScope.drawPath(path, color = Color(0XFF353535))
    }

    drawScope.drawCircle(color = Color(0XFF353535), center = center1, radius = radius)
    drawScope.drawCircle(color = Color(0XFF353535), center = center2, radius = radius)
}

fun Offset.distanceTo(other: Offset): Float {
    return sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
}