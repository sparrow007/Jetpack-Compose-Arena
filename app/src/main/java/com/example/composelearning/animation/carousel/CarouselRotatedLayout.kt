package com.example.composelearning.animation.carousel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.composelearning.animation.CircularCarousel
import com.example.composelearning.animation.book.width
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
private fun CarouselLayout(
    numOfItems: Int,
    modifier: Modifier = Modifier,
    itemFraction: Float = 0.25f,
    carouseLayoutState: CarouseLayoutState = rememberCarouselLayout(),
    contentFactory: @Composable (Int)-> Unit,
) {
    require(numOfItems > 0) {"The number of items must be greater than 0"}
    require(itemFraction > 0f && itemFraction < 1f) {"The item fraction must be between 0 and 1"}

   Layout(modifier = modifier.dragCarousel(state = carouseLayoutState), content = {
       val angleStep = 360f / numOfItems
       repeat(numOfItems) {
           val itemAngle = (carouseLayoutState.angle + angleStep * it).normalizeAngle()
           Box(modifier = Modifier.fillMaxSize()
               .zIndex(if (itemAngle < 180f) 180 - itemAngle else itemAngle - 180f)
               .graphicsLayer {
                   cameraDistance = 12 * density
                   rotationY = itemAngle
                   alpha = if (itemAngle < 90f || itemAngle > 270f) 1f else 0.6f

                   val scale = 1 - 0.2f * when {
                       itemAngle <= 180f -> itemAngle / 180f
                       else -> (360f - itemAngle) / 180f
                   }
                   scaleX = scale
                   scaleY = scale

               }) {
               contentFactory(it)
           }
       }
   }) { measurables, constraints ->
       val itemDimension = constraints.maxHeight * itemFraction

       val itemConstraints = Constraints.fixed(
           width = itemDimension.toInt(),
           height =  itemDimension.toInt()
       )

       val placeables = measurables.map { measurable -> measurable.measure(itemConstraints) }

       layout(constraints.maxWidth, constraints.maxHeight) {
           val availableHorizontalSpace = constraints.maxWidth - itemDimension
           val horizontalOffset = availableHorizontalSpace / 2.0

           val verticalOffset = (constraints.maxHeight - itemDimension).toInt() / 2

           val angleStep = 2.0 * PI / numOfItems.toDouble()

           placeables.forEachIndexed { index, placeable ->
               val itemAngle =  (carouseLayoutState.angle.degreesToRadians() + ((angleStep * index.toDouble()))) % 360

               val offset = getCoordinates(availableHorizontalSpace / 2.0f,
                   height = (constraints.maxHeight / 2.0f - itemDimension) * carouseLayoutState.minorAxisFactor,
                    itemAngle.toFloat())

               placeable.placeRelative(
                   x = (horizontalOffset + offset.x).roundToInt(),
                   y =  offset.y.roundToInt() + verticalOffset
               )
           }
       }
   }

}

fun getCoordinates(width: Float, height: Float, angle: Float): Offset {
    val y = height * cos(angle)
    val x = width * sin(angle)
    return Offset(x, y)
}

private fun Float.normalizeAngle(): Float = (this % 360).let { if (it < 0) it + 360 else it }

private fun Float.degreesToRadians(): Float = (this / 360.0 * 2.0 * PI).toFloat()

@SuppressLint("ModifierFactoryUnreferencedReceiver")
@Composable
private fun Modifier.dragCarousel(
    state: CarouseLayoutState
): Modifier = composed {
    pointerInput(Unit) {
        val decay = splineBasedDecay<Float>(this)
        val degreeInPixels = 180f/size.width.toFloat()
        coroutineScope {
            while (true) {
                val tracker = VelocityTracker()
                val pointerInput = awaitPointerEventScope { awaitFirstDown() }
                state.stop()
                awaitPointerEventScope {
                    val isTopHalf = pointerInput.position.y < size.height / 2
                    val signum = when {
                        isTopHalf -> -1f
                        else -> 1f
                    }

                    drag(pointerInput.id) { change ->
                        val horizontalOffset = state.angle + signum * change.positionChange().x * degreeInPixels
                        launch {
                            state.snapTo(horizontalOffset)
                        }

                        val scaleFactor = state.minorAxisFactor + change.positionChange().y / (size.height/2f)

                        state.setMinorAxisFactor(scaleFactor)

                       tracker.addPosition(change.uptimeMillis, change.position)
                        if (change.positionChange() != Offset.Zero) change.consume()

                        //add the logic for the horizontal drag
                    }

                    val velocity = tracker.calculateVelocity().x
                    launch {
                        val targetAngle = decay.calculateTargetValue(
                            state.angle,
                            velocity * degreeInPixels * signum
                        )
                        state.decayTo(targetAngle, velocity * degreeInPixels * signum)
                    }


                }
            }
        }

    }
}

/***
 * Adding the horizontal drag in the carousel layout
 */
@Stable
private interface CarouseLayoutState {
    val angle: Float
    val minorAxisFactor: Float

    suspend fun stop()

    suspend fun snapTo(angle: Float)

    suspend fun decayTo(angle: Float, velocity: Float)

    fun setMinorAxisFactor(factor: Float)
}

class CarouselRotatedLayoutImpl : CarouseLayoutState {

    private val animator = Animatable(0f)

    private val _eccentrictiy = mutableStateOf(0f)

    override val minorAxisFactor: Float
        get() = _eccentrictiy.value


    private val decayAnimationSpec = FloatSpringSpec(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
    )

    override val angle: Float  get() = animator.value

    override suspend fun stop() {
        animator.stop()
    }

    override suspend fun snapTo(angle: Float) {
        animator.snapTo(targetValue = angle)
    }

    override fun setMinorAxisFactor(factor: Float) {
        this._eccentrictiy.value = factor.coerceIn(-1f, 1f)
    }

    override suspend fun decayTo(angle: Float, velocity: Float) {
        animator.animateTo(
            targetValue = angle,
            initialVelocity = velocity,
            animationSpec = decayAnimationSpec
        )
    }
}

@Composable
private fun rememberCarouselLayout(): CarouseLayoutState = remember {
    CarouselRotatedLayoutImpl()
}

@Composable
@Preview
private fun ShowCarouselLayout() {
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
                CarouselLayout(
                    numOfItems = 24,
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