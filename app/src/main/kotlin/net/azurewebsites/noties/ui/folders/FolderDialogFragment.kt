package net.azurewebsites.noties.ui.folders

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.DialogFragmentFolderBinding
import net.azurewebsites.noties.ui.helpers.getPositiveButton
import net.azurewebsites.noties.ui.helpers.launchAndRepeatWithLifecycle
import net.azurewebsites.noties.ui.helpers.showSoftKeyboard
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class FolderDialogFragment : DialogFragment() {

	private var _binding: DialogFragmentFolderBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<FoldersViewModel>({ requireParentFragment() })
	private val folder by lazy { requireArguments().getParcelable(KEY) ?: FolderEntity() }
	@Inject lateinit var userPreferences: PreferenceStorage

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentFolderBinding.inflate(layoutInflater).apply {
			vm = viewModel
			lifecycleOwner = this@FolderDialogFragment
		}
		return MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(if (folder.id == 0) R.string.new_folder else R.string.edit_folder_name)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button) { _, _ -> viewModel.reset() }
			.setPositiveButton(R.string.save_button, null)
			.create().apply {
				setOnShowListener {
					getButton(AlertDialog.BUTTON_POSITIVE).apply {
						setOnClickListener { insertOrUpdateFolder(folder) }
					}
					if (folder.id != 0) binding.folderName.selectAll()
				}
			}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onStart() {
		super.onStart()
		binding.root.post { binding.folderName.showSoftKeyboard() }
		if (folder.id != 0) {
			binding.folderName.setText(folder.name)
			viewModel.updateFolderName(binding.folderName.text!!)
		}
	}

	override fun onResume() {
		super.onResume()
		launchAndRepeatWithLifecycle {
			viewModel.result.collect {
				when (it) {
					Result.EmptyName -> getPositiveButton().isEnabled = false
					Result.EditingName -> {
						if (binding.input.error != null) binding.input.error = null
						if (!getPositiveButton().isEnabled) getPositiveButton().isEnabled = true
					}
					Result.Success -> {
						requireDialog().dismiss()
						viewModel.reset()
					}
					Result.ErrorDuplicateName -> {
						binding.input.error = getString(R.string.error_message_folder_duplicate)
						getPositiveButton().isEnabled = false
					}
				}
			}
		}
	}

	private fun insertOrUpdateFolder(folder: FolderEntity) {
		if (folder.id == 0) {
			viewModel.insertFolder(FolderEntity(name = viewModel.folderName.value))
		}
		else {
			val updatedFolder = folder.copy(name = viewModel.folderName.value)
			viewModel.updateFolder(updatedFolder)
			if (updatedFolder.id == 1) {
				userPreferences.defaultFolderName = updatedFolder.name
			}
		}
	}

	companion object {
		const val KEY = "folder"

		fun newInstance(folder: FolderEntity) = FolderDialogFragment().apply {
			arguments = bundleOf(KEY to folder)
		}
	}
}