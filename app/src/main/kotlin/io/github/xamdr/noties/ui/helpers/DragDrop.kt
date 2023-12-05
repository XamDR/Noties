@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.xamdr.noties.ui.helpers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridItemScopeImpl.animateItemPlacement
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
fun rememberDragDropState(
	lazyGridState: LazyGridState,
	onMove: (Int, Int) -> Unit
): DragDropState {
	val scope = rememberCoroutineScope()
	val state = remember(lazyGridState) {
		DragDropState(
			state = lazyGridState,
			onMove = onMove,
			scope = scope
		)
	}
	LaunchedEffect(state) {
		while (true) {
			val diff = state.scrollChannel.receive()
			lazyGridState.scrollBy(diff)
		}
	}
	return state
}

fun Modifier.dragContainerForHandle(
	dragDropState: DragDropState,
	key: Any
): Modifier {
	return pointerInput(dragDropState) {
		detectDragGestures(
			onDrag = { change, offset ->
				change.consume()
				dragDropState.onDrag(offset = offset)
			},
			onDragStart = { dragDropState.onDragStartWithKey(key) },
			onDragEnd = { dragDropState.onDragInterrupted() },
			onDragCancel = { dragDropState.onDragInterrupted() }
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableItem(
	dragDropState: DragDropState,
	index: Int,
	modifier: Modifier = Modifier,
	content: @Composable (isDragging: Boolean) -> Unit
) {
	val dragging = index == dragDropState.draggingItemIndex
	val draggingModifier = if (dragging) {
		Modifier
			.zIndex(1f)
			.graphicsLayer {
				translationX = dragDropState.draggingItemOffset.x
				translationY = dragDropState.draggingItemOffset.y
			}
	}
	else if (index == dragDropState.previousIndexOfDraggedItem) {
		Modifier
			.zIndex(1f)
			.graphicsLayer {
				translationX = dragDropState.previousItemOffset.value.x
				translationY = dragDropState.previousItemOffset.value.y
			}
	}
	else {
		Modifier.animateItemPlacement()
	}
	Box(modifier = modifier.then(draggingModifier), propagateMinConstraints = true) {
		content(dragging)
	}
}

class DragDropState(
	private val state: LazyGridState,
	private val scope: CoroutineScope,
	private val onMove: (Int, Int) -> Unit
) {
	var draggingItemIndex by mutableStateOf<Int?>(null)
		private set

	internal val scrollChannel = Channel<Float>()

	private var draggingItemDraggedDelta by mutableStateOf(Offset.Zero)
	private var draggingItemInitialOffset by mutableStateOf(Offset.Zero)
	internal val draggingItemOffset: Offset
		get() = draggingItemLayoutInfo?.let { item ->
			draggingItemInitialOffset + draggingItemDraggedDelta - item.offset.toOffset()
		} ?: Offset.Zero

	private val draggingItemLayoutInfo: LazyGridItemInfo?
		get() = state.layoutInfo.visibleItemsInfo
			.firstOrNull { it.index == draggingItemIndex }

	internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
		private set
	internal var previousItemOffset = Animatable(Offset.Zero, Offset.VectorConverter)
		private set

	internal fun onDragStartWithKey(key: Any) {
		draggingItemIndex = state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }?.index
		draggingItemInitialOffset =
			state.layoutInfo.visibleItemsInfo[draggingItemIndex?.minus(state.firstVisibleItemIndex)
				?: 0].offset.toOffset()
	}

	internal fun onDragInterrupted() {
		if (draggingItemIndex != null) {
			previousIndexOfDraggedItem = draggingItemIndex
			val startOffset = draggingItemOffset
			scope.launch {
				previousItemOffset.snapTo(startOffset)
				previousItemOffset.animateTo(
					Offset.Zero,
					spring(
						stiffness = Spring.StiffnessMediumLow,
						visibilityThreshold = Offset.VisibilityThreshold
					)
				)
				previousIndexOfDraggedItem = null
			}
		}
		draggingItemDraggedDelta = Offset.Zero
		draggingItemIndex = null
		draggingItemInitialOffset = Offset.Zero
	}

	internal fun onDrag(offset: Offset) {
		draggingItemDraggedDelta += offset

		val draggingItem = draggingItemLayoutInfo ?: return
		val startOffset = draggingItem.offset.toOffset() + draggingItemOffset
		val endOffset = startOffset + draggingItem.size.toSize()
		val middleOffset = startOffset + (endOffset - startOffset) / 2f

		val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
			middleOffset.x.toInt() in item.offset.x..item.offsetEnd.x &&
					middleOffset.y.toInt() in item.offset.y..item.offsetEnd.y &&
					draggingItem.index != item.index
		}
		if (targetItem != null) {
			val scrollToIndex = if (targetItem.index == state.firstVisibleItemIndex) {
				draggingItem.index
			} else if (draggingItem.index == state.firstVisibleItemIndex) {
				targetItem.index
			} else {
				null
			}
			if (scrollToIndex != null) {
				scope.launch {
					// this is needed to neutralize automatic keeping the first item first.
					state.scrollToItem(scrollToIndex, state.firstVisibleItemScrollOffset)
					onMove.invoke(draggingItem.index, targetItem.index)
				}
			} else {
				onMove.invoke(draggingItem.index, targetItem.index)
			}
			draggingItemIndex = targetItem.index
		} else {
			val overscroll = when {
				draggingItemDraggedDelta.y > 0 ->
					(endOffset.y - state.layoutInfo.viewportEndOffset).coerceAtLeast(0f)
				draggingItemDraggedDelta.y < 0 ->
					(startOffset.y - state.layoutInfo.viewportStartOffset).coerceAtMost(0f)
				else -> 0f
			}
			if (overscroll != 0f) {
				scrollChannel.trySend(overscroll)
			}
		}
	}

	private val LazyGridItemInfo.offsetEnd: IntOffset
		get() = this.offset + this.size
}

private operator fun IntOffset.plus(size: IntSize): IntOffset {
	return IntOffset(x + size.width, y + size.height)
}

private operator fun Offset.plus(size: Size): Offset {
	return Offset(x + size.width, y + size.height)
}
