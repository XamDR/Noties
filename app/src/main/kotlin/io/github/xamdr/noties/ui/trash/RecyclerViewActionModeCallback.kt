package io.github.xamdr.noties.ui.trash

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.notes.NoteAdapter

class RecyclerViewActionModeCallback(private val adapter: NoteAdapter) : ActionMode.Callback {

	var isVisible = false

	override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
		val inflater = mode.menuInflater
		inflater.inflate(R.menu.recyclerview_trash_context_menu, menu)
		isVisible = true
		return true
	}

	override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = when (item.itemId) {
		R.id.delete_forever -> {
			adapter.deleteNotes(); true
		}
		R.id.restore_note -> {
			adapter.restoreNotes(); true
		}
		else -> false
	}

	override fun onDestroyActionMode(mode: ActionMode) {
		adapter.tracker?.clearSelection()
		isVisible = false
	}
}