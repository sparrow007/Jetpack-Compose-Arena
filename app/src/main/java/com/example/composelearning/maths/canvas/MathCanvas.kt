package com.example.composelearning.maths.canvas

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.pm.ShortcutInfoCompat.Surface

/**
 * Basic setup for the canvas to show and provide multiple functions
 * for drawing the shapes and lines.
 */
@Composable
@Preview
fun MathCanvas() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawCircle(color = Color.Red, radius = 100f)
            }
    )
}