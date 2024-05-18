package com.example.composelearning.animation.colorswaft

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ColorSwitchComposable() {
    MaterialTheme {
        val colors = listOf(
            Color.Blue,
            Color.Red,
            Color.Green,
            Color.Magenta,
            Color.Cyan,
        )
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {

            }
        }
    }
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
private fun Modifier.rotateToSwatch(
    onDrag: (angle: Float) -> Unit
): Modifier = composed {
    var handleCenter by remember {
        mutableStateOf(Offset.Zero)
    }
    pointerInput(Unit) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        detectDragGestures { change, dragAmount ->
            handleCenter += dragAmount
            val centerOffset = Offset(centerX, centerY)
            val changeOffset = Offset(change.position.x - (size.width /2f), change.position.y - size.height)
            val firstAngle = atan2(changeOffset.y, changeOffset.x) * (180f / PI).toFloat()

            val changeSecondOffset = Offset((size.width)/2f - change.position.x, size.height - change.position.y)
            val angleInRadians = (atan2(changeSecondOffset.y, changeSecondOffset.x) * (180f / PI).toFloat())
            val angleInDegrees = Math.toDegrees(angleInRadians.toDouble())

            Log.e("Animation", "angle in degree f = ${90+ firstAngle} and also an second = ${ 90 - (180 - angleInRadians)} ")


           onDrag(90 + firstAngle)
            //change.consume()
        }
    }
}

//create custom modiifier to perform drag animation in the swaft layout
@SuppressLint("ModifierFactoryUnreferencedReceiver")
private fun Modifier.swipeToRotate(
    sizeee: Int,
    onDrag: (angle: Float) -> Unit
): Modifier = composed {

    val rotationAnim = remember {
        Animatable(0f)
    }


    pointerInput(Unit) {
        coroutineScope {
            while (true) {
               // rotationAnim.stop()
                val velocityTracker = VelocityTracker()
                val degreesPerPixel = 180f / size.width.toFloat()

                val center = size.width/2f

              //  val poininter = awaitPointerEventScope { awaitFirstDown().id }

                awaitPointerEventScope {

                   drag(awaitFirstDown().id) { change ->

                        velocityTracker.addPosition(change.uptimeMillis, change.position)

                        val delta = change.position



                       val x = delta.x
                       val y= delta.y
                       val angle =  atan2(y - center, x - center) * (180f / PI).toFloat()

                       val angleInRadians =  if (angle in -180f..0f) {
                           angle + 360f
                       } else {
                           angle
                       }
                       val angleInDegrees = Math.toDegrees(angleInRadians.toDouble())
                          // print("angle in dgree = $angleInDegrees")
//                       Log.e("Animation", "horizontaldrag = ${change.positionChange().x * degreesPerPixel} and the vertical " +
//                               "is = ${change.positionChange().y * degreesPerPixel}")
//
                       Log.e("Animation", "pointer position 1 = ${change.positionChange()}")
//
//
                       Log.e("Animation", "pointer position 2 = ${change.previousPosition}")

                      Log.e("Animation", "angle in degree = ${angleInRadians}")
                       onDrag(angleInRadians.toFloat())
                      // if (change.positionChange() != Offset.Zero) change.consume()

                   }
                }
                val velocity = velocityTracker.calculateVelocity().x
              //  val targetOffset = splineBasedDecay(velocity, animationSpec = FloatSpringSpec(stiffness = Spring.StiffnessLow))
            }
        }
    }
}

@Composable
fun ColorSwaftComposable(colors: List<List<Color>>, modifier: Modifier = Modifier) {

    val rotationAnim = remember {
        Animatable(0f)
    }
    val coroutineScope = rememberCoroutineScope()


    Box (modifier = Modifier.width(260.dp).wrapContentHeight().rotateToSwatch(
    ) { angle ->
        coroutineScope.launch {
            rotationAnim.snapTo(angle)
        }
    }.graphicsLayer {
       // transformOrigin = TransformOrigin.Center
       // rotationZ = 90f
    }.background(color = Color.Gray), contentAlignment = Alignment.CenterStart) {
        colors.forEachIndexed { index, colorList ->

            Box (modifier = Modifier
                .graphicsLayer {
                    cameraDistance = 12 * density
                    transformOrigin = TransformOrigin(0.3f, 0.9f)
                    //translationY = size.height/2f
                    rotationZ = (rotationAnim.value / (colorList.size - 1)) * index
                   // translationY = -(size.height/2f)
                }
            ) {
                ColorSwitchLayout(
                    colors = colorList,
                    modifier = modifier,
                    selectedIndex = 0,
                    onSelectedIndexChanged = {},
                )
            }

        }
    }

}

@Composable
fun ColorSwitchLayout(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
) {

    Card (shape = RoundedCornerShape(18.dp), modifier = Modifier
        .wrapContentSize()
        .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.White
        )) {
        Column (modifier = Modifier.padding(2.dp)) {
            colors.forEachIndexed() { index, color ->

                val (topStart, topEnd, topPadding) = if (index == 0) {
                    Triple(18.dp, 18.dp, 2.dp)
                } else {
                    Triple(5.dp, 5.dp, 3.dp)
                }

                val (bottomStart, bottomEnd) = if (index == colors.size - 1) {
                    Pair(18.dp, 18.dp)
                } else {
                    Pair(5.dp, 5.dp)
                }

                Box(
                    modifier = Modifier
                        .padding(top = topPadding)
                        .size(50.dp)
                        .background(
                            color, shape = RoundedCornerShape(
                                topStart = topStart, topEnd = topEnd,
                                bottomStart = bottomStart, bottomEnd = bottomEnd
                            )
                        )

                ) {}
            }

        }
    }

}

@Preview (showBackground = true, showSystemUi = true)
@Composable
fun ColorSwitchLayoutPreview() {

    val listofColorStack = listOf(
        listOf(
            Color.Blue,
            Color.Red,
            Color.Green,
            Color.LightGray
        ),
        listOf(
            Color.Red,
            Color.Green,
            Color.Magenta,
            Color.Cyan
        ),
        listOf(
            Color.Green,
            Color.Magenta,
            Color.Cyan,
            Color.Blue
        ),
        listOf(
            Color.Magenta,
            Color.Cyan,
            Color.Blue,
            Color.Red
        ),
        listOf(
            Color.Cyan,
            Color.Blue,
            Color.Red,
            Color.Magenta
        ),
        listOf(
            Color.Blue,
            Color.Red,
            Color.Green,
            Color.Gray
        ),
    )

    MaterialTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black), contentAlignment = Alignment.Center) {
                ColorSwaftComposable(
                    colors = listofColorStack,
                )
            }
        }
    }
}


