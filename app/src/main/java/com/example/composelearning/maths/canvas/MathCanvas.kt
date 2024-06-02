package com.example.composelearning.maths.canvas

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.random.Random

/**
 * Basic setup for the canvas to show and provide multiple functions
 * for drawing the shapes and lines.
 */
@Composable
fun MathCanvas() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                for (i in 0..100 step 1) {
                    drawLine(
                        color = Color.Blue,
                        start = Offset(
                            Random.nextFloat() * size.width,
                            Random.nextFloat() * size.height
                        ),
                        end = Offset(
                            Random.nextFloat() * size.width,
                            Random.nextFloat() * size.height
                        ),
                        strokeWidth = 2f
                    )
                }

                drawCircle(color = Color.Red, radius = 100f)
            }
    )
}

@Composable
@Preview
fun MathCanvasSinWave() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                withTransform({
                    this.translate(0f, -size.height / 2f)
                    this.scale(1f, -1f)
                }, {
                    var i = 0.0
                    while (i < 2 * PI) {

                        val x1 = i * 100
                        val y1 = 100 * kotlin.math
                            .sin(i)
                            .toFloat()
                        drawRect(
                            color = Color.Blue,
                            topLeft = Offset(x1.toFloat(), y1),
                            size = Size(100f, 100f)
                        )
                        i += 0.01
                    }

                })
//                translate() {
//
//                    scale(1f, -1f) {
//
////                        val path = Path()
////                        path.moveTo(0f, 0f)
////                        for (i in 0..1000) {
////                            val x = i * 0.1f
////                            val y = sin(x)
////                            path.lineTo(x * 10, y * 10)
////                        }
////                        drawPath(path, Color.Blue)
//                    }
//
//                }


            }
    )
}