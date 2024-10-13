package com.example.composelearning.customlayout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CarouselCustomLayout(
    numberOfItems: Int,
    itemFraction: Float = .25f,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int) -> Unit
) {

    require(numberOfItems > 0) { "Number of items should be greater than 0" }
    require(itemFraction in 0.0..0.5) { "Item fraction should be between 0 and 1" }

    //Todo need measure the size of the child and measure the size of the parent

    Layout(modifier = modifier, content = {
        repeat(numberOfItems) { index ->
            Box(modifier = Modifier.fillMaxSize()) {
                content(index)
            }
        }
    }) { measurables, constraints ->
        val itemDimension = constraints.maxHeight * itemFraction
        val itemConstraints = Constraints.fixed(
            width = itemDimension.toInt(), height = itemDimension.toInt()
        )

        val placeable = measurables.map {
            it.measure(itemConstraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {

            val availableHorizontalSpace = constraints.maxWidth - itemDimension
            val horizontalOffset = availableHorizontalSpace / 2
            val verticalOffset = (constraints.maxHeight - itemDimension) / 2
            val angleStep = 2.0 * PI / numberOfItems.toDouble()

            placeable.forEachIndexed() { index, placeable ->
                val angle = angleStep * index
                val offset = getCoordinates(
                    width = availableHorizontalSpace / 2.0,
                    height = (constraints.maxHeight / 2.0 - itemDimension),
                    angle = angle
                )

                placeable.placeRelative(
                    x = (offset.x + horizontalOffset).toInt(),
                    y = (offset.y + verticalOffset).toInt()
                )

            }
        }

    }
}


@Preview(widthDp = 420, heightDp = 720)
@Composable
private fun PreviewCarouse() {
    MaterialTheme {
        val colors = listOf(
            Color.Blue,
            Color.Red,
            Color.Green,
            Color.Magenta,
            Color.Cyan,
        )
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CarouselCustomLayout(
                    numberOfItems = 24,
                    itemFraction = .2f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = colors[index % colors.size]
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = index.toString(),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

private fun getCoordinates(width: Double, height: Double, angle: Double): Offset {
    val x = width * sin(angle)
    val y = height * cos(angle)
    return Offset(
        x = x.toFloat(),
        y = y.toFloat()
    )
}

//********* Circular Carousel State Handler *********//

@Stable
private interface CircularCarouselState {
    val angle: Float
    suspend fun stop()
    suspend fun snapTo(angle: Float)
    suspend fun decayTo(angle: Float, velocity: Float)
}

class CircularCarouselStateImpl : CircularCarouselState {

    private val _angle = Animatable(0f)

    override val angle: Float
        get() = _angle.value

    private val decayAnimationSpec = FloatSpringSpec(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    override suspend fun stop() {
        _angle.stop()
    }

    override suspend fun snapTo(angle: Float) {
        _angle.snapTo(angle)
    }

    override suspend fun decayTo(angle: Float, velocity: Float) {
        _angle.animateTo(
            targetValue = angle,
            animationSpec = decayAnimationSpec,
            initialVelocity = velocity
        )
    }
}