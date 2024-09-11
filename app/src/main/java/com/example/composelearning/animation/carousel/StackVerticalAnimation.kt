package com.example.composelearning.animation.carousel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun StackCard(colorList: List<Color>, size: Int) {

    val offsetYAnimation = remember {
        Animatable(30f)
    }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        colorList.forEachIndexed { index, color ->
            val density = LocalDensity.current
            val yOffset = ((size - (size - index)) * offsetYAnimation.value)
            //you can use linear interpolation to create scalable value range for 0 to 1
            val scale = 1f - (size - index) * 0.05f
            ShowCards(modifier = Modifier
                .size(180.dp, 230.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = yOffset
                }
                .userDragAction {
                    Log.e("MY TAG", "StackCard: ${offsetYAnimation.value - it}")
                    coroutineScope.launch {
                        offsetYAnimation.animateTo(offsetYAnimation.value + it)
                    }
                }
                , color = color, index = index
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * .offset(y = yOffset)
 *                 .graphicsLayer {
 *                     scaleX = scale
 *                     scaleY = scale
 *                     rotationX = -(index * 3f)
 *                     spotShadowColor = Color.White
 *                     ambientShadowColor = Color.Gray
 *                     cameraDistance = 5f
 *                 }
 */

@SuppressLint("ModifierFactoryUnreferencedReceiver", "UnnecessaryComposedModifier")
fun Modifier.userDragAction(onUserDrag: (dragValue: Float) -> Unit): Modifier = composed {
    val offsetY = remember {
        Animatable(30f)
    }
    pointerInput(Unit) {
        val splineDecay = splineBasedDecay<Float>(this)
        coroutineScope {
            while (true) {
                val velocityTracker = VelocityTracker()
                awaitPointerEventScope {
                    verticalDrag(this.awaitFirstDown().id) { change ->
                        val targetOffset = change.positionChange().y

                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        if (change.positionChange() != Offset.Zero) change.consume()

                        onUserDrag(targetOffset)

                        val velocity = velocityTracker.calculateVelocity().y
                        val targetOffsetY =
                            splineDecay.calculateTargetValue(offsetY.value, velocity)

                    }
                }
            }
        }
    }
}

@Composable
fun ShowCards(modifier: Modifier, color: Color, index: Int) {
    val elevation = with(LocalDensity.current) {
        (index * 100).toDp()
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
            .shadow(elevation = elevation, shape = RoundedCornerShape(10.dp)), // Add this line ,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation,
            draggedElevation = 20.dp,

            )
    ) {}
}

@Composable
fun ShowCardsShadow(modifier: Modifier, color: Color, index: Int) {
    val elevation = with(LocalDensity.current) {
        (index * 20).toDp()
    }
    Box(
        modifier = modifier
            .offset(y = -elevation)
            .fillMaxWidth()
            .height(elevation)
            .background(color = Color.Black.copy(alpha = 0.2f))
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp, // Remove the default elevation
            draggedElevation = 20.dp,
        )
    ) {}
}

@Composable
@Preview(showSystemUi = true)

fun ShowVerticalCardStack() {
    val list = listOf<Color>(
        Color(0xFF99CDF7),
        Color(0xFFFAE294),
        Color(0xFFBA98F7),
        Color(0xFF83F7EC)
    )
    StackCard(list, list.size)
}