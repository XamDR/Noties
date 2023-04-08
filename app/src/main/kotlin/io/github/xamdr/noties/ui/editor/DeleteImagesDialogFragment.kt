package io.github.xamdr.noties.ui.editor

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R

class DeleteImagesDialogFragment : DialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
		.setMessage(R.string.delete_all_images_question)
		.setNegativeButton(R.string.cancel_button, null)
		.setPositiveButton(R.string.ok_button) { _, _ -> onDeleteImagesCallback() }
		.create()

	fun setOnDeleteImagesListener(callback: () -> Unit) {
		onDeleteImagesCallback = callback
	}

	private var onDeleteImagesCallback: () -> Unit = {}
}