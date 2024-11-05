package com.example.composelearning.customlayout

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.composelearning.customlayout.lazylayout.CustomLazyListScope
import com.example.composelearning.customlayout.lazylayout.LazyLayoutState
import com.example.composelearning.customlayout.lazylayout.rememberItemProvider
import com.example.composelearning.customlayout.lazylayout.rememberLazyLayoutState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyLayoutInfiniteScroll(
    modifier: Modifier = Modifier,
    state: LazyLayoutState = rememberLazyLayoutState(),
    content: CustomLazyListScope.() -> Unit
) {

    val itemProvider = rememberItemProvider(content)

    LazyLayout(
        itemProvider = itemProvider,
        modifier = modifier
            .clipToBounds()
            .lazyLayoutPointerInput(state)
    ) { constriants ->
        val viewBoundaries = state.getBoundaries(constraints = constriants)
        val index = itemProvider.getItemIndexInRanges(viewBoundaries)
        val indexsWithPlaceable = index.associateWith {
            measure(it, constriants)
        }
        layout(constriants.maxWidth, constriants.maxHeight) {
            indexsWithPlaceable.forEach { (index, placeable) ->
                val item = itemProvider.getItem(index)
                item?.let {

                }
            }
        }
    }


}



data class ListItem(
    val x: Int,
    val y: Int
)

data class ViewBoundaries(
    val fromX: Int,
    val toX: Int,
    val fromY: Int,
    val toY: Int
)

typealias ComposeItemContent = @Composable (ListItem) -> Unit

class LazyLayoutItemContent(
    val item: ListItem,
    val content: ComposeItemContent
)


@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.lazyLayoutPointerInput(state: LazyLayoutState) = pointerInput(Unit) {
    detectDragGestures { change, dragAmount ->
        change.consume()
        state.onDrag(IntOffset(dragAmount.x.toInt(), dragAmount.y.toInt()))
    }
}




@Composable
fun SnapCarouselLayout(
    numberOfItems: Int,
    modifier: Modifier = Modifier,
    content: @Composable (index: Int) -> Unit
) {
    require(numberOfItems > 0) { "Number of items should be greater than 0" }

    Layout(modifier = modifier, content = {
        repeat(numberOfItems) { index ->
            content(index)
        }
    }) { measurables, constraints ->
        val itemConstraints = Constraints.fixed(
            width = constraints.maxWidth / 2,
            height = constraints.maxHeight / 2
        )

        val placeables = measurables.map { it.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val centerX = constraints.maxWidth / 2
            val centerY = constraints.maxHeight / 2

            placeables.forEachIndexed { index, placeable ->
                val xPosition = when (index) {
                    0 -> 0
                    numberOfItems - 1 -> constraints.maxWidth - placeable.width
                    else -> centerX - placeable.width / 2
                }
                val yPosition = centerY - placeable.height / 2

                placeable.placeRelative(x = xPosition, y = yPosition)
            }
        }
    }
}

@Preview
@Composable
fun PreviewSnapCarouselLayout() {
    SnapCarouselLayout(numberOfItems = 3) { index ->
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 200.dp)
                .background(
                    color = when (index) {
                        0 -> Color.Red
                        1 -> Color.Green
                        else -> Color.Blue
                    }
                )
                .fillMaxSize()
        )
    }
}