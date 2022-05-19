package net.azurewebsites.noties.ui.folders

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.DialogFragmentFolderBinding
import net.azurewebsites.noties.ui.helpers.getPositiveButton
import net.azurewebsites.noties.ui.helpers.showSoftKeyboard
import net.azurewebsites.noties.ui.helpers.toEditable
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class FolderDialogFragment : DialogFragment() {

	private var _binding: DialogFragmentFolderBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<FoldersViewModel>({ requireParentFragment() })
	private val folderUiState by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(KEY) ?: FolderUiState()
	}
	@Inject lateinit var userPreferences: PreferenceStorage
	private var shouldUpdate = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		shouldUpdate = folderUiState.operation == Operation.Update
		if (savedInstanceState != null) {
			shouldUpdate = savedInstanceState.getBoolean(UPDATE)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentFolderBinding.inflate(layoutInflater).apply {
			vm = viewModel
			lifecycleOwner = this@FolderDialogFragment
		}
		return MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(if (folderUiState.operation == Operation.Insert) R.string.new_folder else R.string.update_folder_name)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button) { _, _ -> viewModel.reset() }
			.setPositiveButton(R.string.save_button, null)
			.create().apply {
				setOnShowListener {
					getButton(AlertDialog.BUTTON_POSITIVE).apply {
						setOnClickListener { insertOrUpdateFolder(folderUiState) }
					}
				}
			}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putBoolean(UPDATE, shouldUpdate)
	}

	override fun onStart() {
		super.onStart()
		binding.root.post { binding.folderName.showSoftKeyboard() }
		if (shouldUpdate) {
			viewModel.updateFolderState(folderUiState)
			viewModel.updateFolderName(folderUiState.name.toEditable())
			shouldUpdate = false
		}
	}

	override fun onResume() {
		super.onResume()
		viewModel.inputNameState.observe(this) {
			when (it) {
				InputNameState.EmptyName -> getPositiveButton().isEnabled = false
				InputNameState.EditingName -> {
					if (binding.input.error != null) binding.input.error = null
					if (!getPositiveButton().isEnabled) getPositiveButton().isEnabled = true
				}
				InputNameState.UpdatingName -> {
					binding.folderName.selectAll()
					getPositiveButton().isEnabled = false
				}
				InputNameState.ErrorDuplicateName -> {
					binding.input.error = getString(R.string.error_message_folder_duplicate)
					getPositiveButton().isEnabled = false
				}
			}
		}
	}

	private fun insertOrUpdateFolder(folderUiState: FolderUiState) {
		when (folderUiState.operation) {
			Operation.Insert -> {
				val newFolder = FolderEntity(name = viewModel.folderUiState.value.name)
				viewModel.insertFolder(newFolder)
			}
			Operation.Update -> {
				val updatedFolder = FolderEntity(
					id = folderUiState.id,
					name = viewModel.folderUiState.value.name
				)
				viewModel.updateFolder(updatedFolder)
				if (updatedFolder.id == 1) {
					userPreferences.defaultFolderName = updatedFolder.name
				}
			}
		}
		requireDialog().dismiss()
		viewModel.reset()
	}

	companion object {
		const val KEY = "folder"
		private const val UPDATE = "update"

		fun newInstance(uiState: FolderUiState) = FolderDialogFragment().apply {
			arguments = bundleOf(KEY to uiState)
		}
	}
}