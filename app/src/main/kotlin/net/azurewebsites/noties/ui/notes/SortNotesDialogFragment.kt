package net.azurewebsites.noties.ui.notes

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.noties.R

class SortNotesDialogFragment : DialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		val items = arrayOf("By modification date", "By content", "By title")
		val dialog = MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.sort_notes)
			.setNegativeButton(R.string.cancel_button, null)
			.setItems(items) { _, which ->
				when (which) {
					0 -> onSortNotesCallback(SortMode.LastEdit)
					1 -> onSortNotesCallback(SortMode.Content)
					2 -> onSortNotesCallback(SortMode.Title)
				}
			}
			.create()
		dialog.window?.setWindowAnimations(R.style.ScaleAnimationDialog)
		return dialog
	}

	fun setOnSortNotesListener(callback: (mode: SortMode) -> Unit) {
		onSortNotesCallback = callback
	}

	private var onSortNotesCallback: (mode: SortMode) -> Unit = {}
}