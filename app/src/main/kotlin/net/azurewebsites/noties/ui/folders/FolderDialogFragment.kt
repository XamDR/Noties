package net.azurewebsites.noties.ui.folders

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.DialogFragmentFolderBinding
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.ui.helpers.showSoftKeyboard
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class FolderDialogFragment : DialogFragment() {

	private var _binding: DialogFragmentFolderBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<FoldersViewModel>()
	private lateinit var folder: FolderEntity
	@Inject lateinit var userPreferences: PreferenceStorage

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		folder = requireArguments().getParcelable("directory") ?: FolderEntity()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentFolderBinding.inflate(layoutInflater).apply {
			directory = this@FolderDialogFragment.folder
		}
		return MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(R.string.new_folder)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.save_button, null)
			.show()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onStart() {
		super.onStart()
		binding.root.post { binding.folderName.showSoftKeyboard() }
		binding.folderName.addTextChangedListener(ErrorTextWatcher())
	}

	override fun onResume() {
		super.onResume()
		if (dialog != null) {
			(dialog as AlertDialog)
				.getButton(AlertDialog.BUTTON_POSITIVE)
				.setOnClickListener { insertOrUpdateDirectory(folder) }
		}
		viewModel.setResultListener { succeed ->
			if (succeed) {
				dialog?.dismiss()
			}
			else {
				binding.input.error = getString(R.string.error_message_folder_duplicate)
			}
		}
	}

	private fun insertOrUpdateDirectory(folder: FolderEntity) {
		if (folder.id == 0) {
			viewModel.upsertDirectory(FolderEntity(name = folder.name))
		}
		else {
			val newDirectory = FolderEntity(name = folder.name)
			val updatedDirectory = folder.copy(name = newDirectory.name)
			viewModel.upsertDirectory(updatedDirectory)

			if (folder.id == 1) {
				userPreferences.defaultDirectoryName = folder.name
			}
		}
	}

	private inner class ErrorTextWatcher : TextWatcher {
		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

		override fun afterTextChanged(s: Editable?) {
			if (binding.input.error != null) {
				binding.folderName.removeTextChangedListener(this)
				binding.input.error = null
				binding.folderName.addTextChangedListener(this)
			}
		}
	}

	companion object {
		fun newInstance(folder: FolderEntity) = FolderDialogFragment().apply {
			arguments = bundleOf("directory" to folder)
		}
	}
}