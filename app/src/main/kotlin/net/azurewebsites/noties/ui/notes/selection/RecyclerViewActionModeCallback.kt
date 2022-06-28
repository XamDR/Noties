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

	override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
		if (adapter.getSelectedNotes().any { !it.entity.isProtected }) {
			mode.menu.findItem(R.id.lock_note).apply {
				setIcon(R.drawable.ic_lock_note)
				setTitle(R.string.lock_note)
			}
		}
		else {
			mode.menu.findItem(R.id.lock_note).apply {
				setIcon(R.drawable.ic_unlock_note)
				setTitle(R.string.unlock_note)
			}
		}
		if (adapter.getSelectedNotes().any { !it.entity.isPinned }) {
			mode.menu.findItem(R.id.pin_note).apply {
				setIcon(R.drawable.ic_pin_note)
				setTitle(R.string.pin_note)
			}
		}
		else {
			mode.menu.findItem(R.id.pin_note).apply {
				setIcon(R.drawable.ic_unpin_note)
				setTitle(R.string.unpin_note)
			}
		}
		return true
	}

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = when (item.itemId) {
		R.id.select_all -> {
			adapter.selectAllNotes(); true
		}
		R.id.delete_notes -> {
			adapter.deleteNotes(); true
		}
		R.id.pin_note -> {
			adapter.togglePinnedValueForNotes(); true
		}
		R.id.lock_note -> {
			adapter.toggleLockedValueForNotes(); true
		}
		else -> false
	}

	override fun onDestroyActionMode(mode: ActionMode) {
		adapter.tracker?.clearSelection()
		isVisible = false
	}
}