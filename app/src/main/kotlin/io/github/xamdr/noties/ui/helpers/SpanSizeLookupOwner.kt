package io.github.xamdr.noties.ui.helpers

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Taken from:
// https://github.com/carousell/ConcatAdapterExtension/blob/master/lib/src/main/java/com/carousell/concatadapterextension/ConcatSpanSizeLookup.kt
interface SpanSizeLookupOwner {
	fun getSpanSizeLookup(): GridLayoutManager.SpanSizeLookup
}

class ConcatSpanSizeLookup(
	private val spanCount: Int,
	private val adaptersProvider: () -> List<RecyclerView.Adapter<*>>) : GridLayoutManager.SpanSizeLookup() {

	override fun getSpanSize(position: Int): Int {
		var index = position
		adaptersProvider().forEach { adapter ->
			if (index < adapter.itemCount) {
				return if (adapter is SpanSizeLookupOwner) {
					adapter.getSpanSizeLookup().getSpanSize(index)
				} else spanCount
			}
			index -= adapter.itemCount
		}
		return spanCount
	}
}