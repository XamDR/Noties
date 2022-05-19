package net.azurewebsites.noties.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

// Based on this answer: https://stackoverflow.com/a/27801394/8781554
/**
 * A RecyclerView that supports providing an empty view which
 * is displayed when the adapter has no data and hidden otherwise.
 */
class CollectionView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

	private var emptyView: View? = null

	private val observer = object : AdapterDataObserver() {
		override fun onChanged() = checkIfEmpty()
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkIfEmpty()
		override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkIfEmpty()
	}

	fun setEmptyView(view: View) {
		emptyView = view
		checkIfEmpty()
	}

	override fun setAdapter(adapter: Adapter<*>?) {
		this.adapter?.unregisterAdapterDataObserver(observer)
		adapter?.registerAdapterDataObserver(observer)
		super.setAdapter(adapter)
		checkIfEmpty()
	}

	private fun checkIfEmpty() {
		if (emptyView != null && adapter != null) {
			val emptyViewVisible = adapter?.itemCount == 0
			emptyView?.isVisible = emptyViewVisible
			isVisible = !emptyViewVisible
		}
	}
}