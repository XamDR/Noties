package io.github.xamdr.noties.ui.notes

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.getPositiveButton

class MoveNotesDialogFragment : DialogFragment() {

	private val viewModel by viewModels<NotesViewModel>({ requireParentFragment() })
	private val notes by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableArrayList<Note>(NOTES_KEY) ?: emptyList()
	}
//	private val notebooks by lazy(LazyThreadSafetyMode.NONE) {
//		requireArguments().getParcelableArrayList<Notebook>(NOTEBOOKS_KEY) ?: emptyList()
//	}
	private var notebookId = 0

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//		val items = notebooks.map { it.name }.toTypedArray()
//		val dialog = MaterialAlertDialogBuilder(requireContext())
//			.setTitle(R.string.move_notes_title)
//			.setNegativeButton(R.string.cancel_button, null)
//			.setPositiveButton(R.string.move_button, null)
//			.setSingleChoiceItems(items, -1) { _, which -> selectNotebook(which) }
//			.create().apply {
//				setOnShowListener {
//					getButton(AlertDialog.BUTTON_POSITIVE).apply {
//						setOnClickListener { moveNotes(notes, notebookId) }
//					}
//				}
//			}
//		dialog.window?.setWindowAnimations(R.style.ScaleAnimationDialog)
//		return dialog
		throw NotImplementedError("")
	}

	override fun onResume() {
		super.onResume()
		getPositiveButton().isEnabled = false
	}

	fun setOnNotesMovedListener(callback: () -> Unit) {
		onNotesMovedCallback = callback
	}

	private fun selectNotebook(position: Int) {
//		notebookId = notebooks[position].id
		getPositiveButton().isEnabled = true
	}

	private fun moveNotes(notes: List<Note>, notebookId: Int) {
		viewModel.moveNotes(notes, notebookId) { onNotesMovedCallback() }
		dismiss()
	}

	private var onNotesMovedCallback: () -> Unit = {}

	companion object {
		private const val NOTES_KEY = "notes"
		private const val NOTEBOOKS_KEY = "notebooks"

//		fun newInstance(notes: List<Note>,
//		                notebooks: List<NotebookEntityLocal>) = MoveNotesDialogFragment().apply {
//			arguments = bundleOf(
//				NOTES_KEY to notes,
//				NOTEBOOKS_KEY to notebooks
//			)
//		}
	}
}