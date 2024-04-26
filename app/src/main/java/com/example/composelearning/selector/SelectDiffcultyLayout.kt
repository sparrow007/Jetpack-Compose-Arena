package com.example.composelearning.selector

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.composelearning.animation.animationTime
import kotlinx.coroutines.launch

@Composable
fun SelectorDiffcultyLayout() {

    var currentMode by remember { mutableStateOf(Difficulty.Easy) }

    val rotationXAnim = remember { Animatable(0f) }
    val scaleAnim = remember {
        Animatable(1f)
    }
    val coroutineScope = rememberCoroutineScope()

     fun runAnim(rotationX: Float, scale: Float){
        coroutineScope.launch {
            launch {
                rotationXAnim.animateTo(rotationX,   animationSpec = tween(animationTime))
            }
            launch {
                scaleAnim.animateTo(scale,   animationSpec = tween(animationTime))
            }
        }
    }

    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .graphicsLayer {
                    this.rotationX = rotationXAnim.value
                    this.scaleX = scaleAnim.value
                    this.scaleY = scaleAnim.value

                }
                .size(200.dp),  shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Column (modifier = Modifier
                .wrapContentSize()
                .padding(20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                SelectorButton(20f, Difficulty.Easy, currentMode) { rotationX, scale ->
                    currentMode = Difficulty.Easy
                    runAnim(rotationX, scale)

                }
                Spacer(modifier = Modifier.height(16.dp))
                SelectorButton(0f, Difficulty.Normal, currentMode) { rotationX, scale ->
                    currentMode = Difficulty.Normal
                    runAnim(rotationX, scale)
                }
                Spacer(modifier = Modifier.height(16.dp))
                SelectorButton(-20f, Difficulty.Hard, currentMode) { rotationX, scale ->
                    currentMode = Difficulty.Hard
                    runAnim(rotationX, scale)
                }
            }
        }
    }
}