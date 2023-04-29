package io.github.xamdr.noties.ui.notes.selection

import android.app.Dialog
import android.os.Bundle
import android.text.Spanned
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.getParcelableArrayListCompat

class DeleteNotesDialogFragment : DialogFragment() {

	private val notes by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableArrayListCompat(KEY, Note::class.java)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.delete_notes_title)
			.setMessage(formatMessage(R.string.delete_notes_question))
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ -> onNotesDeletedCallback(notes) }
			.create()
	}

	fun setOnNotesDeletedListener(callback: (notes: List<Note>) -> Unit) {
		onNotesDeletedCallback = callback
	}

	private fun formatMessage(@StringRes resId: Int): Spanned {
		val message = requireContext().getText(resId).toString()
		return HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_COMPACT)
	}

	private var onNotesDeletedCallback: (notes: List<Note>) -> Unit = {}

	companion object {
		private const val KEY = "notes"

		fun newInstance(notes: List<Note>) = DeleteNotesDialogFragment().apply {
			arguments = bundleOf(KEY to ArrayList(notes))
		}
	}
}