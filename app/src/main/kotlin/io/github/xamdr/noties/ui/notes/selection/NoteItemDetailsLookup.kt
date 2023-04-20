package io.github.xamdr.noties.ui.notes.selection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.notes.NoteAdapter

class NoteItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Note>() {

	override fun getItemDetails(e: MotionEvent): ItemDetails<Note>? {
		val view = recyclerView.findChildViewUnder(e.x, e.y)
		if (view != null) {
			val viewHolder = recyclerView.getChildViewHolder(view) as NoteAdapter.BaseViewHolder
			return viewHolder.getItemDetails()
		}
		return null
	}
}