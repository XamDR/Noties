package net.azurewebsites.eznotes.ui.folders

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.core.DirectoryEntity
import net.azurewebsites.eznotes.databinding.FragmentFolderDialogBinding
import net.azurewebsites.eznotes.ui.helpers.showSoftKeyboard
import net.azurewebsites.eznotes.ui.settings.UserPreferences

class FolderDialogFragment : DialogFragment() {

	private var _binding: FragmentFolderDialogBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<FolderListViewModel>()
	private lateinit var directory: DirectoryEntity
	private lateinit var userPreferences: UserPreferences

	override fun onAttach(context: Context) {
		super.onAttach(context)
		userPreferences = UserPreferences(context)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		directory = requireArguments().getParcelable("directory") ?: DirectoryEntity()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = FragmentFolderDialogBinding.inflate(layoutInflater).apply {
			directory = this@FolderDialogFragment.directory
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
				.setOnClickListener { insertOrUpdateDirectory(directory) }
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

	private fun insertOrUpdateDirectory(directory: DirectoryEntity) {
		if (directory.id == 0) {
			viewModel.upsertDirectory(DirectoryEntity(name = directory.name))
		}
		else {
			val newDirectory = DirectoryEntity(name = directory.name)
			val updatedDirectory = directory.copy(name = newDirectory.name)
			viewModel.upsertDirectory(updatedDirectory)

			if (directory.id == 1) {
				userPreferences.defaultDirectoryName = directory.name
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
		fun newInstance(directory: DirectoryEntity) = FolderDialogFragment().apply {
			arguments = bundleOf("directory" to directory)
		}
	}
}