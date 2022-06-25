package net.azurewebsites.noties.ui.notes

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SortNotesDialogFragment : DialogFragment() {

	@Inject lateinit var preferenceStorage: PreferenceStorage
	private var checkedItem by Delegates.notNull<Int>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		checkedItem = when (preferenceStorage.sortMode) {
			SortMode.Content.name -> 0
			SortMode.LastEdit.name -> 1
			SortMode.Title.name -> 2
			else -> -1
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		val dialog = MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.sort_notes)
			.setNegativeButton(R.string.cancel_button, null)
			.setSingleChoiceItems(R.array.sort_mode, checkedItem) { dialog, which ->
				when (which) {
					0 -> onSortNotesCallback(SortMode.Content)
					1 -> onSortNotesCallback(SortMode.LastEdit)
					2 -> onSortNotesCallback(SortMode.Title)
				}
				dialog.dismiss()
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