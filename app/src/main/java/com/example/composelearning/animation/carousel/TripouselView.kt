package com.example.composelearning.animation.carousel

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.composelearning.R
import com.example.composelearning.animation.CircularCarousel
import com.example.composelearning.animation.book.width
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
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
                      color = Color(0xff90caf9),
                      imagePath = R.drawable.cover_maga_f
                  ),
                  ItemData(
                      color = Color(0xfffafafa),
                      imagePath = R.drawable.cover_mag_sec
                  ),
                  ItemData(
                      color = Color(0xffef9a9a),
                      imagePath = R.drawable.cover_mag_th
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
                          //There is no new composable it's only the laid one which is
                          // getting recomposed based the key to identify the composable

                          ItemView(
                              data = data,
                              modifier = Modifier
                                  .swipeToDissmiss {
                                      val itemData = ItemData(
                                          color = data.color,
                                          id = data.id,
                                          imagePath = data.imagePath
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
    val color: Color,
    @DrawableRes val imagePath: Int
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

    val animationTiming = 500

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
            }
            .graphicsLayer {
                cameraDistance = 12 * density
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
            defaultElevation = 12.dp
        )
    ) {
        Image(painter = painterResource(id = data.imagePath), contentDescription = "magazine image")

    }
}

@Composable
private fun Modifier.swipeToDissmiss(
    onDismiss: ()->Unit
): Modifier = composed {
    val offsetAnimX = remember {
        Animatable(0f)
    }
    val offsetAnimY = remember {
        Animatable(0f)
    }
    val scaleAnim = remember {
        Animatable(1f)
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenWidth = LocalDensity.current.run { screenWidthDp.dp.toPx() }

    pointerInput(Unit) {
        val decay = splineBasedDecay<Float>(this)
        coroutineScope {

            while (true) {
                val velocityTracker = VelocityTracker()
                awaitPointerEventScope {
                    drag(awaitFirstDown().id) { change ->
                        val horizontalOffset = offsetAnimX.value + change.positionChange().x
                        val verticalOffset = offsetAnimY.value + change.positionChange().y
                        launch {
                            offsetAnimX.snapTo(horizontalOffset)
                        }
                        launch {
                            offsetAnimY.snapTo(verticalOffset)
                        }
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        if (change.positionChange() != Offset.Zero) {
                            change.consume()
                        }

                    }


                    val velocity = velocityTracker.calculateVelocity().x
                    val targetOffsetX = decay.calculateTargetValue(offsetAnimX.value, velocity)
                    //  val targetOffsetY = decay.calculateTargetValue(offsetAnimY.value, velocity)
                    //set animation bounds to screen width

                    launch {
                        if (targetOffsetX.absoluteValue <= size.width) {
                            launch {
                                offsetAnimX.animateTo(targetValue = 0f)
                            }
                            launch {
                                offsetAnimY.animateTo(0f)
                            }
                        } else {
                            val target =
                                if (targetOffsetX > 0) size.width * 2f else -size.width * 2f
                            offsetAnimX.animateTo(
                                targetValue = target,
                                animationSpec = tween(durationMillis = 200)
                            )
                            onDismiss()
                            listOf(
                                launch {
                                    offsetAnimX.snapTo(0f,)
                                },
                                launch {
                                    offsetAnimY.snapTo(0f,)
                                }
                            ).joinAll()

                        }
                    }
                }
            }
        }
    }
        .offset {
            IntOffset(offsetAnimX.value.roundToInt(), offsetAnimY.value.roundToInt())
        }
        .graphicsLayer {

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