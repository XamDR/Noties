package net.azurewebsites.noties.ui.notes

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

// Based on these articles:
// https://www.journaldev.com/23164/android-recyclerview-swipe-to-delete-undo
// https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e
class SwipeToDeleteCallback(private val adapter: NoteAdapter) :
	ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {

	override fun onMove(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		target: RecyclerView.ViewHolder
	): Boolean = false

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
		if (viewHolder is NoteAdapter.NoteViewHolder) {
			val position = viewHolder.bindingAdapterPosition
			adapter.moveNoteToTrash(position)
		}
	}
}