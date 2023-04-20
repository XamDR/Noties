package io.github.xamdr.noties.ui.image

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.DialogImageDescriptionBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.editor.EditorViewModel
import io.github.xamdr.noties.ui.helpers.getPositiveButton
import io.github.xamdr.noties.ui.helpers.getSerializableCompat
import io.github.xamdr.noties.ui.helpers.showSoftKeyboard
import io.github.xamdr.noties.ui.helpers.toEditable

class ImageDescriptionDialogFragment : DialogFragment() {

	private var _binding: DialogImageDescriptionBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>({ requireParentFragment() })
	private val image by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getSerializableCompat(KEY, Image::class.java)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogImageDescriptionBinding.inflate(layoutInflater)
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.alt_text)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.save_button, null)
			.create().apply {
				setOnShowListener {
					getButton(AlertDialog.BUTTON_POSITIVE).apply {
						setOnClickListener { updateImageAltText() }
					}
				}
			}
	}

	override fun onStart() {
		super.onStart()
		if (viewModel.description.value.isEmpty()) {
			image.description?.let { viewModel.updateImageAltText(it.toEditable()) }
		}
		binding.root.post {
			binding.imageDesc.showSoftKeyboard()
			binding.imageDesc.setSelection(viewModel.description.value.length)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onResume() {
		super.onResume()
		viewModel.altTextState.observe(this) {
			when (it) {
				AltTextState.EmptyDescription -> getPositiveButton().isEnabled = false
				AltTextState.EditingDescription ->
					if (!getPositiveButton().isEnabled) getPositiveButton().isEnabled = true
			}
		}
	}

	fun setOnAltTextAddedListener(callback: () -> Unit) {
		onAltTextAddedCallback = callback
	}

	private fun updateImageAltText() {
		viewModel.updateImage(image, viewModel.description.value) { onAltTextAddedCallback() }
		requireDialog().dismiss()
	}

	private var onAltTextAddedCallback: () -> Unit = {}

	companion object {
		private const val KEY = "image"

		fun newInstance(image: Image) = ImageDescriptionDialogFragment().apply {
			arguments = bundleOf(KEY to image)
		}
	}
}