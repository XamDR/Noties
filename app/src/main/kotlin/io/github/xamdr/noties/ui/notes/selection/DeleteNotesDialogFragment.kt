package io.github.xamdr.noties.ui.notes.selection

import android.app.Dialog
import android.os.Bundle
import android.text.Spanned
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R
import io.github.xamdr.noties.core.Note
import io.github.xamdr.noties.ui.notes.NotesViewModel

class DeleteNotesDialogFragment : DialogFragment() {

	private val viewModel by viewModels<NotesViewModel>({ requireParentFragment() })
	private val notes by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableArrayList<Note>(KEY) ?: emptyList()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.delete_notes_title)
			.setMessage(formatMessage(R.string.delete_notes_question))
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ ->
				viewModel.deleteNotes(notes) { onNotesDeletedCallback() }
			}
			.create()
	}

	fun setOnNotesDeletedListener(callback: () -> Unit) {
		onNotesDeletedCallback = callback
	}

	private fun formatMessage(@StringRes resId: Int): Spanned {
		val message = requireContext().getText(resId).toString()
		return HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT)
	}

	private var onNotesDeletedCallback: () -> Unit = {}

	companion object {
		private const val KEY = "notes"

		fun newInstance(notes: List<Note>) = DeleteNotesDialogFragment().apply {
			arguments = bundleOf(KEY to ArrayList(notes))
		}
	}
}