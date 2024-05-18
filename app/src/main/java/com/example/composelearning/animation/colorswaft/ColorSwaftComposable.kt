package com.example.composelearning.animation.colorswaft

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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

//                ColorSwaftComposable(
//                    colors = colors,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                )
            }
        }
    }
}

@Composable
fun ColorSwaftComposable(colors: List<List<Color>>, modifier: Modifier = Modifier) {

    Box {
        colors.forEachIndexed { index, colorList ->

            Box (modifier = Modifier.graphicsLayer {
                transformOrigin =  TransformOrigin(0f, 1f)
                 rotationZ = (90f / (colors.size - 1)) * index
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
        ),
        listOf(
            Color.Red,
            Color.Green,
            Color.Magenta,
        ),
        listOf(
            Color.Green,
            Color.Magenta,
            Color.Cyan,
        ),
        listOf(
            Color.Magenta,
            Color.Cyan,
            Color.Blue,
        ),
        listOf(
            Color.Cyan,
            Color.Blue,
            Color.Red,
        ),
        listOf(
            Color.Blue,
            Color.Red,
            Color.Green
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
