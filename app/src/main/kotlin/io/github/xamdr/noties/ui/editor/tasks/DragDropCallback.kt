package io.github.xamdr.noties.ui.editor.tasks

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.RecyclerView

class DragDropCallback :
	ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

	override fun onMove(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		target: RecyclerView.ViewHolder
	): Boolean {
		if (viewHolder is TaskAdapter.TaskViewHolder &&
			target is TaskAdapter.TaskViewHolder) {
//			val adapter = (recyclerView.adapter as ConcatAdapter).adapters[1] as TodoItemAdapter
			val adapter = viewHolder.bindingAdapter as TaskAdapter
			val from = viewHolder.bindingAdapterPosition
			val to = target.bindingAdapterPosition
			adapter.moveItem(from, to)
			return true
		}
		return false
	}

	override fun isLongPressDragEnabled() = false

	override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
		super.onSelectedChanged(viewHolder, actionState)
		if (actionState == ACTION_STATE_DRAG) {
			viewHolder?.itemView?.alpha = 0.5f
		}
	}

	override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
		super.clearView(recyclerView, viewHolder)
		viewHolder.itemView.alpha = 1.0f
	}

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }
}