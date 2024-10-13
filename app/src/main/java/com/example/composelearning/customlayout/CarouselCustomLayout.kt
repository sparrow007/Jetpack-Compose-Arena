package com.example.composelearning.customlayout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CarouselCustomLayout(
    numberOfItems: Int,
    itemFraction: Float = .25f,
    modifier: Modifier = Modifier,
    state: CircularCarouselState = rememberCircularCarouselState(),
    content: @Composable (index: Int) -> Unit
) {

    require(numberOfItems > 0) { "Number of items should be greater than 0" }
    require(itemFraction in 0.0..0.5) { "Item fraction should be between 0 and 1" }

    //Todo need measure the size of the child and measure the size of the parent

    Layout(modifier = modifier.dragCarousel(state), content = {
        val angleSteps = 360f / numberOfItems
        repeat(numberOfItems) { index ->
            val itemAngle = (state.angle + angleSteps * index).normalizeAngle()
            Box(modifier = Modifier
                .fillMaxSize()
                .zIndex(if (itemAngle <= 180f) 180f - itemAngle else itemAngle - 180f)
                .graphicsLayer {
                    cameraDistance = 12f * density
                    rotationY = itemAngle
                    alpha = if (itemAngle < 90f || itemAngle > 270f) 1f else .6f

                    val scale = 1f - .2f * when {
                        itemAngle <= 180f -> itemAngle / 180f
                        else -> (360f - itemAngle) / 180f
                    }
                    // 3
                    scaleX = scale
                    scaleY = scale


                }
            ) {
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

            placeable.forEachIndexed { index, placeable ->

                val itemAngle = (state.angle.toDouble().degreesToRadians() + (angleStep * index))

                //   val angle = angleStep * index
                val offset = getCoordinates(
                    width = availableHorizontalSpace / 2.0,
                    height = (constraints.maxHeight / 2.0 - itemDimension) * state.minorAxisFactor,
                    angle = itemAngle
                )

                placeable.placeRelative(
                    x = (offset.x + horizontalOffset).toInt(),
                    y = (offset.y + verticalOffset).toInt()
                )

            }
        }

    }
}

private fun Double.degreesToRadians(): Double = this / 360.0 * 2.0 * PI

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
interface CircularCarouselState {
    val angle: Float

    val minorAxisFactor: Float

    suspend fun stop()
    suspend fun snapTo(angle: Float)
    suspend fun decayTo(angle: Float, velocity: Float)

    fun setMinorAxisFactor(factor: Float)
}

class CircularCarouselStateImpl : CircularCarouselState {

    private val _eccentricity = mutableFloatStateOf(1f)

    override val minorAxisFactor: Float
        get() = _eccentricity.floatValue

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

    override fun setMinorAxisFactor(factor: Float) {
        _eccentricity.floatValue = factor.coerceIn(-1f, 1f)
    }
}

@Composable
fun rememberCircularCarouselState(): CircularCarouselState {
    return remember { CircularCarouselStateImpl() }
}

private fun Modifier.dragCarousel(
    state: CircularCarouselState
) = pointerInput(Unit) {
    val decay = splineBasedDecay<Float>(this)
    coroutineScope {
        while (true) {
            val pointerInput = awaitPointerEventScope { awaitFirstDown() }
            state.stop()

            val tracker = VelocityTracker()

            val degreePerPixel = 180 / size.width.toFloat()

            awaitPointerEventScope {

                val isTopHalf = pointerInput.position.y < size.height / 2
                val signum = when {
                    isTopHalf && state.minorAxisFactor >= 0f -> -1f
                    isTopHalf -> 1f
                    state.minorAxisFactor >= 0f -> 1f
                    else -> 1f
                }

                drag(pointerId = pointerInput.id) { change ->
                    val horizontalDragOffset =
                        state.angle + change.positionChange().x * degreePerPixel * signum

                    launch {
                        state.snapTo(horizontalDragOffset)
                    }

                    val scaleFactor =
                        state.minorAxisFactor + change.positionChange().y / (size.height / 2f)
                    state.setMinorAxisFactor(scaleFactor)

                    tracker.addPosition(change.uptimeMillis, change.position)
                    if (change.positionChange() == Offset.Zero) {
                        change.consume()
                    }
                }

                val velocity = tracker.calculateVelocity().x
                val targetAngle = decay.calculateTargetValue(
                    state.angle,
                    velocity * degreePerPixel * signum
                )

                launch {
                    state.decayTo(targetAngle, velocity * degreePerPixel * signum)
                }
            }

        }
    }
}

private fun Float.normalizeAngle(): Float =
    (this % 360f).let { angle -> if (this < 0f) 360f + angle else angle }
