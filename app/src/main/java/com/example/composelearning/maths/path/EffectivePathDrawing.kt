package com.example.composelearning.maths.path

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview

@Preview(showSystemUi = true)
@Composable
fun CreateNormalPath() {

    var animState by remember {
        mutableStateOf(150f)
    }
    val scale = animateFloatAsState(
        targetValue =
        if (animState == 150f) 80f else 150f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )
    Column(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()) {
            val path = Path()
            path.moveTo(0f, 150f)
            //path.lineTo(0f, 150f)
            path.quadraticBezierTo(size.width / 2, scale.value, size.width, 150f)
            this.drawPath(path, Color.Blue, style = Stroke(5f))
        }

        Button(onClick = {
            animState = if (animState == 150f) 80f else 150f
        }) {
            Text(text = "Animate")
        }

    }

}