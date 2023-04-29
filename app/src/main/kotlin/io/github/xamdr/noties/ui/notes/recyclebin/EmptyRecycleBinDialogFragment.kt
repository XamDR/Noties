package io.github.xamdr.noties.ui.notes.recyclebin

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R

class EmptyRecycleBinDialogFragment : DialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
		.setTitle(R.string.empty_trash_question)
		.setMessage(R.string.empty_trash_message_warning)
		.setNegativeButton(R.string.cancel_button, null)
		.setPositiveButton(R.string.yes_button) { _, _ -> onRecycleBinEmptyCallback() }
		.create()

	fun setOnRecycleBinEmptyListener(callback: () -> Unit) {
		onRecycleBinEmptyCallback = callback
	}

	private var onRecycleBinEmptyCallback: () -> Unit = {}
}