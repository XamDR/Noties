package net.azurewebsites.noties.ui.trash

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.noties.R

class EmptyRecycleBinDialogFragment : DialogFragment() {

	private val viewModel by viewModels<RecycleBinViewModel>({ requireParentFragment() })

	override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
		.setTitle(R.string.empty_trash_question)
		.setMessage(R.string.empty_trash_message_warning)
		.setNegativeButton(R.string.cancel_button, null)
		.setPositiveButton(R.string.yes_button) { _, _ -> viewModel.emptyRecycleBin(onRecycleBinEmptyCallback) }
		.create()

	fun setOnRecycleBinEmptyListener(callback: () -> Unit) {
		onRecycleBinEmptyCallback = callback
	}

	private var onRecycleBinEmptyCallback: () -> Unit = {}
}