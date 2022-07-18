package net.azurewebsites.noties.ui.editor

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Note

class MenuItemClickListener(
	private val listener: ToolbarItemMenuListener,
	private val note: Note) : Toolbar.OnMenuItemClickListener {

	override fun onMenuItemClick(item: MenuItem) = when (item.itemId) {
		R.id.share_content -> {
			listener.shareContent(); true
		}
		R.id.delete_images -> {
			listener.showDeleteImagesDialog(); true
		}
		R.id.open_file -> {
			listener.openTextFile(); true
		}
		R.id.hide_todos -> {
			listener.hideTodoList(); true
		}
		R.id.lock_note -> {
			toggleNoteLockedStatus(item); true
		}
		R.id.pin_note -> {
			toggleNotePinnedStatus(item); true
		}
		else -> false
	}

	private fun toggleNoteLockedStatus(item: MenuItem) {
		if (note.entity.isProtected) {
			listener.unlockNote()
			item.apply {
				setIcon(R.drawable.ic_lock_note)
				setTitle(R.string.lock_note)
			}
		}
		else {
			listener.lockNote()
			item.apply {
				setIcon(R.drawable.ic_unlock_note)
				setTitle(R.string.unlock_note)
			}
		}
	}

	private fun toggleNotePinnedStatus(item: MenuItem) {
		if (note.entity.isPinned) {
			listener.unpinNote()
			item.apply {
				setIcon(R.drawable.ic_pin_note)
				setTitle(R.string.pin_note)
			}
		}
		else {
			listener.pinNote()
			item.apply {
				setIcon(R.drawable.ic_unpin_note)
				setTitle(R.string.unpin_note)
			}
		}
	}
}