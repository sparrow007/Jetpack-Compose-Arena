package com.example.composelearning.animation.carousel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
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
import java.util.UUID
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TripouselView(
    modifier: Modifier = Modifier.offset { IntOffset.Zero },
) {
    val listOfCard = remember {
          mutableStateOf(
              listOf<ItemData>(
                  ItemData(
                      color = Color(0xff90caf9)
                  ),
                  ItemData(
                      color = Color(0xfffafafa)
                  ),
                  ItemData(
                      color = Color(0xffef9a9a)
                  ),
              )
          )
    }
    var showList by remember {
        mutableStateOf(true)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {

        Column {
            if (showList) {
               Box(modifier = Modifier
                   .fillMaxWidth()
                   .wrapContentHeight()
                   .padding(5.dp), contentAlignment = Alignment.Center) {

                   listOfCard.value.forEachIndexed { index, data ->
                      key(data.id) {
                          ItemView(
                              data = data,
                              modifier = Modifier
                                  .swipeToDissmiss {
                                      val itemData = ItemData(
                                          color = data.color,
                                          id = data.id
                                      )
                                      listOfCard.value = listOf(itemData) + (listOfCard.value - data)
                                  },
                              index = index
                          )
                      }

                   }
               }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { showList = !showList }) {
                Text("Show the List")
            }
        }


    }

}

data class ItemData(
    val id: String = UUID.randomUUID().toString(),
    val color: Color
)

private fun getItemProperties(index: Int): Triple<Float, Float, Float> {
    return when(index) {
        0 -> {
            Triple(0.8f, -22f, 1f)
        }

        1 -> {
            Triple(0.8f, 22f, 2f)
        }
        else -> {
            Triple(1f, 0f, 3f)
        }
    }

}

private fun getItemOffset(index: Int): IntOffset {
    return when(index) {
        0 -> {
            IntOffset(-170, 35)
        }
        1 -> {
            IntOffset(170, 35)
        }
        else -> {
            IntOffset(0, 0)
        }
    }
}

@Composable
private fun ItemView(modifier: Modifier, data: ItemData, index: Int) {

    val animationTiming = 300

    val animateOffset = animateIntOffsetAsState(targetValue = getItemOffset(index),
        label = "",
        animationSpec = tween(animationTiming)
    )
    val animateRotation = animateFloatAsState(
        targetValue = getItemProperties(index).second,
        animationSpec = tween(animationTiming), label = ""
    )
    val animateScale = animateFloatAsState(targetValue = getItemProperties(index).first,
        animationSpec = tween(animationTiming), label = ""
    )

    Card (
        modifier = modifier
            .offset {
                animateOffset.value
            }.graphicsLayer {
                rotationZ = animateRotation.value
                scaleX = animateScale.value
                scaleY = animateScale.value
            }
            .size(width = 210.dp, height = 280.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = data.color
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {

    }
}

@Composable
private fun Modifier.swipeToDissmiss(
    onDismiss: ()->Unit
): Modifier = composed {
    val offsetAnim = remember {
        Animatable(0f)
    }
    pointerInput(Unit) {
        val decay = splineBasedDecay<Float>(this)
        coroutineScope {
            while (true) {
                val velocityTracker = VelocityTracker()
                awaitPointerEventScope {
                    horizontalDrag(awaitFirstDown().id) { change ->
                        val horizontalOffset = offsetAnim.value + change.positionChange().x
                        launch {
                            offsetAnim.snapTo(horizontalOffset)
                        }
                        velocityTracker.addPosition(change.uptimeMillis, change.position)

                    }
                    val velocity = velocityTracker.calculateVelocity().x
                    val targetOffsetX = decay.calculateTargetValue(offsetAnim.value, velocity)
                    launch {
                        if (targetOffsetX.absoluteValue <= size.width) {
                            offsetAnim.animateTo(targetValue = 0f)
                        } else {
                            offsetAnim.animateDecay(velocity, decay) {
                            }
                            offsetAnim.snapTo(0f)
                            onDismiss()

                        }
                    }

                }
            }
        }
    }.offset {
        IntOffset(offsetAnim.value.roundToInt(), 0)
    }
}

@Preview (showSystemUi = true)
@Composable
private fun ShowTripouselView() {
    MaterialTheme {
        Surface {
           Box(modifier = Modifier
               .fillMaxSize()
               .background(Color.Black)) {
               Column {
                   Spacer(modifier = Modifier.height(40.dp))
                   TripouselView()
               }
           }
        }
    }
}