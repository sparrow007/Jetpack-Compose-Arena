package com.example.composelearning.animation.carousel.sensorlayout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.composelearning.animation.pillShape
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt

/****
 * Create a custom layout to support the stack based layout in compose
 * lEARNING THE LAZY LAYOUT
 */


@Preview
@Composable
fun ShowCardInStackPreview() {
   MaterialTheme {
       ShowCardInStack()
   }
}
@Composable
private fun ShowCardInStack(
) {
    var defaultColor by remember {
        mutableStateOf(Color(0xff90caf9))
    }
    val animateColor = animateColorAsState(targetValue = defaultColor, label = "color animation")
    val listOfCard = remember {
        mutableStateOf(
            arrayListOf(
                Color(0xff90caf9),
                Color(0xfffafafa),
                Color(0xffef9a9a),
                Color(0xfffff59d),
                Color(0xFFAEF89D),
                Color(0xFFF58FCC),
                Color(0xFFAFFCDD),
                Color(0xFF90caf9),
                Color(0xFFF58FCC),
                Color(0xFFAFFCDD),
                Color(0xFF90caf9),
            ).reversed()
        )
    }

    var checked by remember {
        mutableStateOf(false)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 20.dp)
        .drawBehind {
            drawRect(animateColor.value)
        }) {
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp)
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(modifier = Modifier
                .wrapContentSize()) {
                listOfCard.value.forEachIndexed { index, color ->
                    key(color) {
                        val swipeDismiss = {
                            val ind = listOfCard.value.indexOf(color)
                            val defIndex = if (ind == 0) 3 else ind - 1
                            defaultColor = listOfCard.value[defIndex]
                            listOfCard.value = listOf(color) + (listOfCard.value - color)
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                        CardWithColors(
                            modifier = Modifier
                                .offset {
                                    val pixel = 70.dp.toPx()
                                    IntOffset(
                                        0,
                                        ((((listOfCard.value.size - index) * -pixel) + pixel).toInt())
                                    )
                                }
                                .graphicsLayer {
                                    cameraDistance = 12 * density
                                    val size = listOfCard.value.size - 1
                                    val scaleValue = 1f - (listOfCard.value.size - (index)) * 0.05f
                                    rotationX = -(70f / size) * index
                                    scaleX = scaleValue
                                    scaleY = scaleValue
                                },
                            color = color
                        )
                    }
                }
            }
        }

    }

}

@Composable
private fun CardWithColors(modifier: Modifier,color: Color) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(10.dp),
        colors =   CardDefaults.cardColors(
            containerColor = color, //Card background color
            //contentColor = Color.White  //Card content color,e.g.text
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
            Row (modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp) ) {
                Box (modifier = Modifier
                    .size(50.dp)
                    .pillShape())
                Column (modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))  {
                    Box (modifier = Modifier
                        .width(100.dp)
                        .height(15.dp)
                        .pillShape())
                    Spacer(modifier = Modifier.height(10.dp))
                    Box (modifier = Modifier
                        .width(70.dp)
                        .height(15.dp)
                        .pillShape())

                }
            }
        }
    }
}