package io.github.xamdr.noties.ui.notes.selection

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.notes.NoteAdapter

class RecyclerViewActionModeCallback(private val adapter: NoteAdapter) : ActionMode.Callback {

	var isVisible = false

	override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
		val inflater = mode.menuInflater
		inflater.inflate(R.menu.recyclerview_context_menu, menu)
		isVisible = true
		return true
	}

	override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
		return true
	}

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = when (item.itemId) {
		R.id.select_all -> {
			adapter.selectAllNotes(); true
		}
		R.id.delete_notes -> {
			adapter.deleteNotes(); true
		}
		else -> false
	}

	override fun onDestroyActionMode(mode: ActionMode) {
		adapter.tracker?.clearSelection()
		isVisible = false
		onActionModeDone()
	}

	fun setOnActionModeDoneListener(callback: () -> Unit) {
		onActionModeDone = callback
	}

	private var onActionModeDone: () -> Unit = {}
}