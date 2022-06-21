package net.azurewebsites.noties.ui.image

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.update
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.databinding.DialogImageDescriptionBinding
import net.azurewebsites.noties.ui.editor.EditorViewModel
import net.azurewebsites.noties.ui.helpers.showSoftKeyboard

class ImageDescriptionDialogFragment : DialogFragment() {

	private var _binding: DialogImageDescriptionBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>({ requireParentFragment() })
	private val image by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(KEY) ?: ImageEntity()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogImageDescriptionBinding.inflate(layoutInflater).apply {
			vm = viewModel
			lifecycleOwner = this@ImageDescriptionDialogFragment
		}
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.alt_text)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.save_button) { _, _ ->
				viewModel.updateImage(image, viewModel.description.value)
			}
			.create()
	}

	override fun onStart() {
		super.onStart()
		image.description?.let { viewModel.description.update { it } }
		binding.root.post { binding.imageDesc.showSoftKeyboard() }
	}

	companion object {
		private const val KEY = "image"

		fun newInstance(image: ImageEntity) = ImageDescriptionDialogFragment().apply {
			arguments = bundleOf(KEY to image)
		}
	}
}