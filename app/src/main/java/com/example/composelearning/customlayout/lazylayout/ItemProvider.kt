package com.example.composelearning.customlayout.lazylayout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.composelearning.customlayout.LazyLayoutItemContent
import com.example.composelearning.customlayout.ListItem
import com.example.composelearning.customlayout.ViewBoundaries


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberItemProvider(customLazyListScope: CustomLazyListScope.() -> Unit): ItemProvider {
    val customLazyListScopeState = remember {
        mutableStateOf(customLazyListScope)
    }

    return remember {
        ItemProvider(
            derivedStateOf {
                val listScope = CustomLazyListScopeImpl().apply(customLazyListScopeState.value)
                listScope.items
            }
        )
    }
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