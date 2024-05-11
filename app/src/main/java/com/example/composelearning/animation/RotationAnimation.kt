package com.example.composelearning.animation

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.ui.theme.fontFamily
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val animationTime: Int = 400

@Composable
fun ViewRoatation(modifier: Modifier = Modifier, rotationX: Float, rotationY: Float, rotationZ: Float,
                  scale: Float, isPressed: Boolean) {

    val fontWeight by animateIntAsState(
        targetValue = if (isPressed) FontWeight.Bold.weight else FontWeight.Normal.weight,
        animationSpec = tween(durationMillis = animationTime), label = "font weight animation"
    )
    val colorAnimate by animateColorAsState(targetValue = if (isPressed) Color.Black else Color.Gray,
        animationSpec = tween(durationMillis = animationTime), label = "color animation"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(), contentAlignment = Alignment.Center) {
        Card(modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationY
                this.rotationZ = rotationZ
              ///  this.rotationY = rotationY
               // this.shadowElevation = 10f
                this.scaleX = scale
                this.scaleY = scale

                // this.cameraDistance = camerDistance
            }
            .size(200.dp)
            , colors = CardDefaults.cardColors(
                containerColor = Color.White, //Card background color
                //contentColor = Color.White  //Card content color,e.g.text
            ), shape = RoundedCornerShape(20.dp)) {
                Text(text ="Hard",
                    style = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight(fontWeight)
                    , fontSize = 22.sp),
                   color = colorAnimate,
                    modifier = Modifier.padding(20.dp)
                )
        }
    }
}

@Composable
fun ShowSliderToExperiment() {
    var rotationY by remember { mutableStateOf(0f) }
    val rotationX = remember { Animatable(0f) }
    var rotationZ by remember { mutableStateOf(0f) }
    var cameraDistance by remember { mutableStateOf(0f) }
    var isPressed by remember {
        mutableStateOf(false)
    }
    val scale = remember {
        Animatable(1f)
    }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            ViewRoatation(modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        // This is called when the touch down event is received
                        coroutineScope.launch {
                            println("Touch down at $offset")
                           // rotationX = 20f
                           // rotationY = 0f
                            launch {
                                rotationX.animateTo(20f,
                                    animationSpec = tween(animationTime)
                                )
                            }
                            launch {
                                scale.animateTo(0.8f, animationSpec = tween(animationTime))
                            }
                            isPressed = false

                            delay(animationTime.toLong())
                            tryAwaitRelease()

                            isPressed = true

                            launch {
                                scale.animateTo(1f, animationSpec = tween(animationTime))
                            }

                            launch {
                                rotationX.animateTo(0f, animationSpec = tween(animationTime))
                            }
                            //rotationX = 0f
                           // rotationY = 0f
                            // This is called when the touch up event is received
                            println("Touch up at $offset")
                        }
                    }
                )
            }
                , rotationX.value, rotationY, rotationZ, scale.value, isPressed)

            Spacer(modifier = Modifier.height(20.dp))

            Slider(value = rotationY, onValueChange = { it -> rotationY = it }, valueRange = -360f..360f)

            Spacer(modifier = Modifier.height(10.dp))

            Slider(value = rotationY, onValueChange = { it ->  },  valueRange = -360f..360f)
            Spacer(modifier = Modifier.height(10.dp))

            Slider(value = rotationZ, onValueChange = { it -> rotationZ = it },  valueRange = -360f..360f)

            Spacer(modifier = Modifier.height(10.dp))

            Slider(value = cameraDistance, onValueChange = { it -> cameraDistance = it },  valueRange = 0f..30f)
        }
    }

}



@Composable
@Preview
fun ShowRotationPreview() {
    MaterialTheme {
       ShowSliderToExperiment()
    }
}