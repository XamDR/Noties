package net.azurewebsites.noties.ui.folders

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
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
	private val folder by lazy { requireArguments().getParcelable(KEY) ?: FolderEntity() }
	@Inject lateinit var userPreferences: PreferenceStorage
	private val textWatcher = InputTextWatcher()

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentFolderBinding.inflate(layoutInflater).apply {
			folder = this@FolderDialogFragment.folder
		}
		return MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(if (folder.id == 0) R.string.new_folder else R.string.edit_folder_name)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.save_button, null)
			.create().apply {
				setOnShowListener {
					getButton(AlertDialog.BUTTON_POSITIVE).apply {
						isEnabled = false
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
		binding.folderName.addTextChangedListener(textWatcher)
	}

	override fun onStop() {
		super.onStop()
		binding.folderName.removeTextChangedListener(textWatcher)
	}

	override fun onResume() {
		super.onResume()
		viewModel.setResultListener { succeed ->
			if (succeed) {
				dialog?.dismiss()
			}
			else {
				binding.input.error = getString(R.string.error_message_folder_duplicate)
			}
		}
	}

	private fun insertOrUpdateFolder(folder: FolderEntity) {
		if (folder.id == 0) {
			viewModel.upsertFolder(FolderEntity(name = folder.name))
		}
		else {
			val newDirectory = FolderEntity(name = folder.name)
			val updatedDirectory = folder.copy(name = newDirectory.name)
			viewModel.upsertFolder(updatedDirectory)

			if (folder.id == 1) {
				userPreferences.defaultFolderName = folder.name
			}
		}
	}

	private inner class InputTextWatcher : TextWatcher {
		override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
			(dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
		}

		override fun afterTextChanged(s: Editable) {
			binding.input.error = if (binding.input.error != null) null else return
		}
	}

	companion object {
		const val KEY = "folder"
	}
}