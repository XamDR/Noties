package net.azurewebsites.noties.ui.notes.selection

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionTracker
import dagger.hilt.android.internal.managers.FragmentComponentManager
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Note

class SelectionObserver(
	private val context: Context,
	private val callback: RecyclerViewActionModeCallback,
	private val tracker: SelectionTracker<Note>) : SelectionTracker.SelectionObserver<Note>() {

	var actionMode: ActionMode? = null

	override fun onSelectionChanged() {
		val numSelectedItems = tracker.selection.size()
		val title = context.resources.getQuantityString(
			R.plurals.notes_selected,
			numSelectedItems,
			numSelectedItems
		)
		if (tracker.hasSelection() && actionMode == null) {
			actionMode = (FragmentComponentManager.findActivity(context) as AppCompatActivity).startSupportActionMode(callback)
			actionMode?.title = title
		}
		else if (!tracker.hasSelection() && actionMode != null) {
			actionMode?.finish()
			actionMode = null
		}
		else {
			actionMode?.title = title
			actionMode?.invalidate()
		}
	}
}