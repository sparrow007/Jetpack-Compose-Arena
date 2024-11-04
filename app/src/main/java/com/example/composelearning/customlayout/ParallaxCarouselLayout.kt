package com.example.composelearning.customlayout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@Composable
fun LazyLayoutInfiniteScroll(
    modifier: Modifier = Modifier,

) {

}

@ExperimentalFoundationApi
class ItemProvider(
    private val itemState: State<List<LazyLayoutItemContent>>
) : LazyLayoutItemProvider {

    override val itemCount: Int
        get() = itemState.value.size

    @Composable
    override fun Item(index: Int, key: Any) {
        val item = itemState.value.getOrNull(index)
        item?.content?.invoke(item.item)
    }

    fun getItemIndexInRanges(viewBoundaries: ViewBoundaries): List<Int> {
        val result = mutableListOf<Int>()
        itemState.value.forEachIndexed { index, item ->
            if (item.item.x in viewBoundaries.fromX..viewBoundaries.toX &&
                item.item.y in viewBoundaries.fromY..viewBoundaries.toY
            ) {
                result.add(index)
            }
        }
        return result
    }

    fun getItem(index: Int): ListItem? {
        return itemState.value.getOrNull(index)?.item
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