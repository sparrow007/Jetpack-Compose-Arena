package com.example.composelearning.animation.colorswaft

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2

@SuppressLint("ModifierFactoryUnreferencedReceiver")
private fun Modifier.rotateToSwatch(
    onDrag: (angle: Float) -> Unit
): Modifier = composed {

    var dragAngle = remember {
        0f
    }

    pointerInput(Unit) {
        detectDragGestures(onDragEnd = {
            val finalAngle = if (dragAngle > 100) 90f else 0f
            onDrag(finalAngle)
        }) { change, dragAmount ->

            val changeSecondOffset = Offset( change.position.x, size.height - change.position.y)
            val angleInRadians = (atan2(changeSecondOffset.y, changeSecondOffset.x) * (180f / PI).toFloat())
            dragAngle = 90 - angleInRadians
            onDrag(dragAngle)
            change.consume()
        }
    }
}

@Composable
fun ColorSwaftComposable(colors: List<List<Color>>, onColorSelected: (color: Color) -> Unit) {

    val rotationAnim = remember {
        Animatable(0f)
    }
    val coroutineScope = rememberCoroutineScope()


    Box (modifier = Modifier
        .width(260.dp)
        .wrapContentHeight()
        .rotateToSwatch(
        ) { angle ->
            coroutineScope.launch {
                val finalAngle = if (angle > 160f) 90f else if (angle < -60f) 0f else angle

                rotationAnim.animateTo(
                    finalAngle,
                    animationSpec = SpringSpec(
                        stiffness = Spring.StiffnessLow,  // The stiffness of the spring
                        dampingRatio = Spring.DampingRatioMediumBouncy  // The damping ratio of the spring
                    )
                )

            }
        }, contentAlignment = Alignment.TopStart) {
        colors.forEachIndexed { index, colorList ->

            Box (modifier = Modifier
                .graphicsLayer {
                    cameraDistance = 12 * density
                    transformOrigin = TransformOrigin(0.3f, 0.9f)
                    //translationY = size.height/2f
                    rotationZ = (rotationAnim.value / (colors.size - 1)) * index
                   // translationY = -(size.height/2f)
                }
            ) {
                ColorSwitchLayout(
                    colors = colorList,
                    onColorSelected = onColorSelected
                )
            }

        }
    }
}

@Composable
fun ColorSwitchLayout(
    colors: List<Color>,
    onColorSelected: (color: Color) -> Unit
) {

    Card (shape = RoundedCornerShape(18.dp), modifier = Modifier
        .wrapContentSize(),
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
                        .clickable {
                            onColorSelected.invoke(color)
                        }

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
            Color(0xFF3DA4FF),
            Color(0xFF7ABFFA),
            Color(0xFFAED7FA),
            Color(0xFFCFE6FA),
        ),
        listOf(
            Color(0xFFFF7536),
            Color(0xFFFFA67D),
            Color(0xFFFFBC9E),
            Color(0xFFFFCCB4),
        ),
        listOf(
            Color(0xFF07C4C4),
            Color(0xFF77FCFC),
            Color(0xFF9AF8F8),
            Color(0xFFD1FDFD),
        ),
        listOf(
            Color(0xFFB52BFF),
            Color(0xFFC459FD),
            Color(0xFFD48DFA),
            Color(0xFFE7BFFD),
        ),
        listOf(
            Color(0xFFFFCC11),
            Color(0xFFFFDA55),
            Color(0xFFFFE893),
            Color(0xFFFFF3C6),
        ),
        listOf(
            Color(0xFF0B0111),
            Color(0xFF2B2E2A),
            Color(0xFF454643),
            Color(0xFF7D807C),
        ),
    )

    var backgroundColor by remember {
        mutableStateOf(Color.Black)
    }

    val animateColor = animateColorAsState(targetValue = backgroundColor, label = "Color Animation")

    MaterialTheme {
        Surface(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            Box(modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(color = animateColor.value)
                }, contentAlignment = Alignment.BottomStart) {
                Box(modifier = Modifier.wrapContentSize().padding(
                    start = 40.dp, end = 20.dp, bottom = 70.dp, top = 20.dp
                )) {
                    ColorSwaftComposable(
                        colors = listofColorStack,
                    ) {
                        backgroundColor = it
                    }
                }
            }
        }
    }
}


