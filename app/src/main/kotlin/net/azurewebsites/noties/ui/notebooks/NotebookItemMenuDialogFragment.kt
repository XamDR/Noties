package net.azurewebsites.noties.ui.notebooks

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity

class NotebookItemMenuDialogFragment : DialogFragment() {

	private val viewModel by viewModels<NotebooksViewModel>({ requireParentFragment() })
	private val notebook by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(NotebooksFragment.NOTEBOOK) ?: Notebook()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val items = listOf(
			NotebookContextMenuItem(R.drawable.ic_edit_notebooks, getString(R.string.edit_notebook_name)),
			NotebookContextMenuItem(R.drawable.ic_delete, getString(R.string.delete_notebook))
		)
		val dialog = MaterialAlertDialogBuilder(requireContext())
			.setTitle(notebook.entity.name)
			.setAdapter(NotebookItemContextMenuAdapter(requireContext(), R.layout.notebook_item_context_menu, items)) { _, which ->
				when (which) {
					0 -> editNotebookName(notebook.entity)
					1 -> deleteNotebook(notebook)
				}
			}
			.setNegativeButton(R.string.cancel_button, null)
			.create()
		dialog.window?.setWindowAnimations(R.style.ScaleAnimationDialog)
		return dialog
	}

	private fun editNotebookName(notebook: NotebookEntity) {
		val uiState = NotebookUiState(id = notebook.id, name = notebook.name, operation = Operation.Update)
		val notebookDialog = NotebookDialogFragment.newInstance(uiState)
		notebookDialog.show(parentFragmentManager, TAG)
	}

	private fun deleteNotebook(notebook: Notebook) {
		viewModel.deleteNotebookAndNotes(notebook) { onShowSnackbarCallback() }
	}

	fun setShowSnackbarListener(callback: () -> Unit) {
		onShowSnackbarCallback = callback
	}

	private var onShowSnackbarCallback: () -> Unit = {}

	companion object {
		fun newInstance(notebook: Notebook) = NotebookItemMenuDialogFragment().apply {
			arguments = bundleOf(NotebooksFragment.NOTEBOOK to notebook)
		}
		private const val TAG = "NOTEBOOK_DIALOG"
	}
}