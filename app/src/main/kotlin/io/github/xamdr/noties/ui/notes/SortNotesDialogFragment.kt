package io.github.xamdr.noties.ui.notes

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SortNotesDialogFragment : DialogFragment() {

	@Inject lateinit var preferenceStorage: PreferenceStorage
	private var checkedItem by Delegates.notNull<Int>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val sortMode = SortMode.valueOf(preferenceStorage.sortMode)
		checkedItem = when (sortMode) {
			SortMode.Content -> 0
			SortMode.LastEdit -> 1
			SortMode.Title -> 2
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