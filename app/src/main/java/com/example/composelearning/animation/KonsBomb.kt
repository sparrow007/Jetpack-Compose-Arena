package com.example.composelearning.animation


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        Modifier.padding(start = 200.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Boing()
                    }
                }
            }
        }
    }
}

@Composable
fun Boing() {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val minDampingRatio = Spring.DampingRatioLowBouncy
    val maxDampingRatio = Spring.DampingRatioHighBouncy

    val minStiffness = Spring.StiffnessLow
    val maxStiffness = 3000f

    val defSize = 34.dp
    val maxWidth = defSize * 10

    val defSizeFloat = remember(defSize) {
        with(density) { defSize.toPx() }
    }

    val maxWidthFloat = remember(maxWidth) {
        with(density) { maxWidth.toPx() }
    }

    val progress = remember { Animatable(0f) }
    var baseColors by remember { mutableStateOf(colors.random()) }
    val colorA = lerp(LocalContentColor.current, baseColors.second, progress.value.coerceAtLeast(0f))
    val colorB = lerp(LocalContentColor.current, baseColors.first, progress.value.coerceAtLeast(0f))
    val brush = Brush.horizontalGradient(listOf(colorA, colorB))

    val size = DpSize(
        width = with(density) { (defSizeFloat + (maxWidthFloat - defSizeFloat) * progress.value).toDp() },
        height = defSize
    )

    Row(
        modifier = Modifier.pointerInput(Unit) {
            forEachGesture {
                awaitPointerEventScope {
                    val startX = awaitFirstDown().position.x
                    baseColors = colors.random()
                    do {
                        val event = awaitPointerEvent()
                        val currentX = event.changes.last().position.x
                        val diffX = currentX - startX
                        // Inspired by PullRefreshState.kt
                        val linearProgress = (diffX / maxWidthFloat).coerceIn(0f, 2f)
                        val stressedProgress = linearProgress - linearProgress.pow(2)/4

                        scope.launch {
                            progress.snapTo(stressedProgress)
                        }
                    } while (event.changes.none { it.changedToUp() })

                    scope.launch {
                        val dampingRatio = minDampingRatio + (maxDampingRatio - minDampingRatio) * progress.value
                        val stiffness = minStiffness + (maxStiffness - minStiffness) * progress.value
                        progress.animateTo(0f, spring(dampingRatio = dampingRatio, stiffness = stiffness))
                    }
                }
            }
        }
    ) {
        BoingText(text = "B")
        StretchyO(brush, size)
        BoingText(text = "ing")
    }
}

@Composable
fun RowScope.StretchyO(
    brush: Brush,
    size: DpSize,
) {
    Surface(
        modifier = Modifier
            .size(size)
            .alignBy { it.measuredHeight },
        border = BorderStroke(5.dp, brush),
        contentColor = Color.Transparent,
        shape = CircleShape
    ) {}
}

@Composable
fun RowScope.BoingText(text: String) {
    Text(
        modifier = Modifier.alignByBaseline(),
        text = text,
        fontSize = 64.sp
    )
}

@Composable
@Preview(showSystemUi = false)
fun ShowBOMB() {
    MaterialTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                Modifier.padding(start = 200.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Boing()
            }
        }
    }
}

private val colors = listOf(
    Color(0xFFEF5350) to Color(0xFFFF1744),
    Color(0xFFAB47BC) to Color(0xFFD500F9),
    Color(0xFF5C6BC0) to Color(0xFF3D5AFE),
    Color(0xFF26A69A) to Color(0xFF1DE9B6),
    Color(0xFF9CCC65) to Color(0xFF76FF03),
    Color(0xFFFFEE58) to Color(0xFFFFEA00),
    Color(0xFFFFA726) to Color(0xFFFF9100),
)