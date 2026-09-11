package com.example.composelearning.basic

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedVisibilitySample() {
    var visible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(24.dp)) {
        Button(onClick = {
            visible = !visible
        }) {
            Text(
                text = if (visible) "show" else "hide"
            )
        }

        Spacer(modifier = Modifier.height(56.dp))

        AnimatedVisibility(visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            DisposableEffect(Unit) {

                Log.d("AnimatedVisibility", "ENTERED composition")

                onDispose {
                    Log.d("AnimatedVisibility", "REMOVED from composition")
                }
            }


            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.Blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Animated Content",
                    color = Color.White
                )
            }
        }
    }

}