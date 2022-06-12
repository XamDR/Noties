package net.azurewebsites.noties.ui.notes.selection

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.notes.NoteAdapter

class RecyclerViewActionModeCallback(private val adapter: NoteAdapter) : ActionMode.Callback {

	var isVisible = false

	override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
		val inflater = mode.menuInflater
		inflater.inflate(R.menu.recyclerview_context_menu, menu)
		isVisible = true
		return true
	}

	override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = when (item.itemId) {
		R.id.select_all -> {
			adapter.selectAllNotes(); true
		}
		R.id.delete_notes -> {
			adapter.deleteNotes(); true
		}
		R.id.lock_note -> {
			adapter.lockNotes(); true
		}
		else -> false
	}

	override fun onDestroyActionMode(mode: ActionMode) {
		adapter.tracker?.clearSelection()
		isVisible = false
	}
}