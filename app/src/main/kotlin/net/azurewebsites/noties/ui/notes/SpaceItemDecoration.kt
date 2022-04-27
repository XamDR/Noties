package net.azurewebsites.noties.ui.notes

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class SpaceItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State
	) {
		val lm = parent.layoutManager as StaggeredGridLayoutManager
		val lp = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

		val spanCount = lm.spanCount
		val spanIndex = lp.spanIndex
		val positionLayout = lp.viewLayoutPosition
		val itemCount = lm.itemCount
		val position = parent.getChildAdapterPosition(view)

		outRect.right = spacing / 4
		outRect.left = spacing / 4
		outRect.top = spacing
		outRect.bottom = spacing

		if (spanIndex == 0) outRect.left = spacing

		if (position < spanCount) outRect.top = spacing

		if (spanIndex == (spanCount - 1)) outRect.right = spacing

		if (positionLayout > (itemCount - spanCount)) outRect.bottom = spacing

	}
}