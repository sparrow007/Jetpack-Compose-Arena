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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
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

/**
 * Curently in Hold need to more time, circle part restricted the animation and view to increase in scaleability
 * but really happy to see this and in future i  will post something on this,
 * it will be very limited to numbers but might look good
 */

@Composable
private fun CarouselLayout(
    numberOfItems: Int,
    modifier: Modifier = Modifier,
) {
    val colors = listOf(
        Color.Blue,
        Color.Red,
        Color.Green,
        Color.Magenta,
        Color.Cyan,
    )

    Layout(
        content = {
            for (i in 0 until numberOfItems) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = colors[i % colors.size])
                ) {
                    Text(
                        text = "Item $i",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
            }
        },
        modifier = modifier
    ) { measurables, constraints ->

        val itemDimension = constraints.maxHeight * 0.2f
        val itemConstraint = Constraints.fixed(
            width = itemDimension.toInt(),
            height = itemDimension.toInt()
        )

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val itemChildWidth = placeables[0].width
            val itemChildHeight = placeables[0].height

            val availableHorizontalSpace = constraints.maxWidth - 2*itemChildWidth


            //Starting rendering the item from the center of the layout
            val horizontalOffset = (constraints.maxWidth - itemDimension) / 2
            val verticalOffset = (constraints.maxHeight - itemChildHeight) / 2

            val angle = 2 * PI / numberOfItems

            placeables.forEachIndexed {index, placeable ->

                val itemAngle = angle * index

                val coordinates = getCarouselCoordinates(
                    width = (availableHorizontalSpace / 2).toFloat(),
                    height = (constraints.maxHeight/2 - 2*itemChildHeight).toFloat(),
                    angle = itemAngle.toFloat()
                )

                placeable.placeRelative(
                    x = (horizontalOffset + coordinates.x).toInt(),
                    y = (verticalOffset + coordinates.y).toInt()
                )
            }
        }
    }
}


private fun getCarouselCoordinates(width: Float, height: Float, angle: Float): Offset {
    val y = height * cos(angle)
    val x = width * sin(angle)
    return Offset(y, x)
}

@Preview
@Composable
fun ShowOwnCarouselLayout() {
    MaterialTheme {

        Surface(modifier = Modifier.fillMaxSize()) {
            Row {

            }
            LazyRow {

            }
            Box(modifier = Modifier
                .wrapContentSize()
                .padding(10.dp), contentAlignment = Alignment.Center) {
                CarouselLayout(numberOfItems = 24)
            }
        }
    }
}