package io.github.xamdr.noties.ui.notes.selection

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionTracker
import dagger.hilt.android.internal.managers.FragmentComponentManager
import io.github.xamdr.noties.core.Note
import io.github.xamdr.noties.ui.helpers.printDebug

class SelectionObserver(
	private val context: Context,
	private val callback: ActionMode.Callback,
	private val tracker: SelectionTracker<Note>) : SelectionTracker.SelectionObserver<Note>() {

	var actionMode: ActionMode? = null

	override fun onSelectionChanged() {
		val numSelectedItems = tracker.selection.size()
		printDebug("NUM_SELECTED_ITEMS", numSelectedItems)

		if (tracker.hasSelection() && actionMode == null) {
			actionMode = (FragmentComponentManager.findActivity(context) as AppCompatActivity).startSupportActionMode(callback)
			actionMode?.title = numSelectedItems.toString()
		}
		else if (!tracker.hasSelection() && actionMode != null) {
			actionMode?.finish()
			actionMode = null
		}
		else {
			actionMode?.title = numSelectedItems.toString()
			actionMode?.invalidate()
		}
	}
}