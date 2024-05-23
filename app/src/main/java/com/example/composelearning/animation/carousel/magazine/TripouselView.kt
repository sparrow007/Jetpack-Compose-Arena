package com.example.composelearning.animation.carousel.magazine

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.example.composelearning.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun TripouselView(
    modifier: Modifier = Modifier.offset { IntOffset.Zero },
    centerViewColor: (color: Int) -> Unit
) {
    val listOfCard = remember {
          mutableStateOf(
              listOf<ItemData>(
                  ItemData(
                      color = Color(0xffef9a9a),
                      imagePath = R.drawable.cover_mag_th
                  ),
                  ItemData(
                      color = Color(0xff90caf9),
                      imagePath = R.drawable.cover_mag_fi
                  ),
                  ItemData(
                      color = Color(0xfffafafa),
                      imagePath = R.drawable.cover_mag_sec
                  ),

              )
          )
    }
    var showList by remember {
        mutableStateOf(true)
    }

    Box(modifier = modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.TopCenter) {

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
        Image(painter = painterResource(id = data.imagePath), contentDescription = "magazine image",
            contentScale = ContentScale.FillBounds)

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
                   TripouselView{

                   }
               }
           }
        }
    }
}

fun extractImageColor(resources: Resources, @DrawableRes drawableId: Int): Int {
    val bitmap = BitmapFactory.decodeResource(resources, drawableId)
    var color = 0
    Palette.from(bitmap).generate { palette ->
        // Use the Palette instance here

        val vibrantColor = palette?.vibrantSwatch?.rgb  // Vibrant color
        val vibrantDarkColor = palette?.darkVibrantSwatch?.rgb  // Vibrant dark color
        val vibrantLightColor = palette?.lightVibrantSwatch?.rgb  // Vibrant light color
        val mutedColor = palette?.mutedSwatch?.rgb  // Muted color
        val mutedDarkColor = palette?.darkMutedSwatch?.rgb  // Muted dark color
        val mutedLightColor = palette?.lightMutedSwatch?.rgb  // Muted light color
        color = vibrantColor ?: 0
    }

    return color
}