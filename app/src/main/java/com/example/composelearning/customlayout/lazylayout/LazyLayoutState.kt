package com.example.composelearning.customlayout.lazylayout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import com.example.composelearning.customlayout.ViewBoundaries


@Composable
fun rememberLazyLayoutState(): LazyLayoutState {
    return remember {
        LazyLayoutState()
    }
}

@Stable
class LazyLayoutState {

    private val _offsetState = mutableStateOf(IntOffset(0, 0))
    val offsetState = _offsetState

    fun onDrag(delta: IntOffset) {
        val x = (_offsetState.value.x - delta.x).coerceAtLeast(0)
        val y = (_offsetState.value.y - delta.y).coerceAtLeast(0)
        _offsetState.value = IntOffset(x, y)
    }

    fun getBoundaries(
        constraints: Constraints,
        threshold: Int = 500
    ): ViewBoundaries {
        return ViewBoundaries(
            fromX = offsetState.value.x - threshold,
            toX = constraints.maxWidth + offsetState.value.x + threshold,
            fromY = offsetState.value.y - threshold,
            toY = constraints.maxHeight + offsetState.value.y + threshold
        )
    }


}