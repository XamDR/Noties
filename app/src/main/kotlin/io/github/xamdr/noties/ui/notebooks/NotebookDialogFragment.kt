package io.github.xamdr.noties.ui.notebooks

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.core.NotebookEntity
import io.github.xamdr.noties.databinding.DialogFragmentNotebookBinding
import io.github.xamdr.noties.ui.MainActivity
import io.github.xamdr.noties.ui.helpers.getPositiveButton
import io.github.xamdr.noties.ui.helpers.showSoftKeyboard
import io.github.xamdr.noties.ui.helpers.toEditable

@AndroidEntryPoint
class NotebookDialogFragment : DialogFragment() {

	private var _binding: DialogFragmentNotebookBinding? = null
	private val binding get() = _binding!!
	private val viewModel by activityViewModels<NotebooksViewModel>()
	private val notebookUiState by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(KEY) ?: NotebookUiState()
	}
	private var shouldUpdate = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		shouldUpdate = notebookUiState.operation == Operation.Update
		if (savedInstanceState != null) {
			shouldUpdate = savedInstanceState.getBoolean(UPDATE)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentNotebookBinding.inflate(layoutInflater).apply {
//			vm = viewModel
//			lifecycleOwner = this@NotebookDialogFragment
		}
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(if (notebookUiState.operation == Operation.Insert) R.string.new_notebook else R.string.edit_notebook_name)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button) { _, _ -> cleanup() }
			.setPositiveButton(R.string.save_button, null)
			.create().apply {
				setOnShowListener {
					getButton(AlertDialog.BUTTON_POSITIVE).apply {
						setOnClickListener { createOrUpdateNotebook(notebookUiState) }
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
		binding.root.post { binding.notebookName.showSoftKeyboard() }
		if (shouldUpdate) {
			viewModel.updateNotebookState(notebookUiState)
			viewModel.updateNotebookName(notebookUiState.name.toEditable())
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
					binding.notebookName.selectAll()
					getPositiveButton().isEnabled = false
				}
				InputNameState.ErrorDuplicateName -> {
					binding.input.error = getString(R.string.error_message_notebook_duplicate)
					getPositiveButton().isEnabled = false
				}
			}
		}
	}

	override fun onDismiss(dialog: DialogInterface) {
		super.onDismiss(dialog)
		cleanup()
	}

	private fun createOrUpdateNotebook(notebookUiState: NotebookUiState) {
		when (notebookUiState.operation) {
			Operation.Insert -> {
				val newNotebook = NotebookEntity(name = viewModel.notebookUiState.value.name)
				viewModel.createNotebook(newNotebook)
			}
			Operation.Update -> {
				val updatedNotebook = NotebookEntity(
					id = notebookUiState.id,
					name = viewModel.notebookUiState.value.name
				)
				viewModel.updateNotebook(updatedNotebook)
			}
		}
		requireDialog().dismiss()
		cleanup()
	}

	private fun cleanup() {
		viewModel.reset()
		(requireActivity() as MainActivity).unlockDrawerLayout()
	}

	companion object {
		const val KEY = "notebook"
		private const val UPDATE = "update"

		fun newInstance(uiState: NotebookUiState) = NotebookDialogFragment().apply {
			arguments = bundleOf(KEY to uiState)
		}
	}
}