package net.azurewebsites.noties.ui.notebooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.databinding.FragmentNotebooksBinding
import net.azurewebsites.noties.ui.helpers.addMenuProvider
import net.azurewebsites.noties.ui.helpers.showDialog
import net.azurewebsites.noties.ui.helpers.showSnackbar
import net.azurewebsites.noties.ui.helpers.tryNavigate

@AndroidEntryPoint
class NotebooksFragment : Fragment(), NotebookToolbarItemListener, NotebookItemContextMenuListener {

	private var _binding: FragmentNotebooksBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<NotebooksViewModel>()
	private val notebookAdapter = NotebookAdapter(this)
	private val menuProvider = NotebooksMenuProvider(this)

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentNotebooksBinding.inflate(inflater, container, false)
		addMenuProvider(menuProvider, viewLifecycleOwner)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.folders.adapter = notebookAdapter
		viewModel.notebooks.observe(viewLifecycleOwner) { notebookAdapter.submitList(it) }
		onBackPressed()
	}

	override fun showCreateNotebookDialog() {
		val notebookDialog = NotebookDialogFragment.newInstance(NotebookUiState())
		showDialog(notebookDialog, TAG)
	}

	override fun showContextMenu(notebook: Notebook) {
		val itemMenuDialog = NotebookItemMenuDialogFragment.newInstance(notebook).apply {
			setShowSnackbarListener { binding.root.showSnackbar(R.string.delete_notes_warning) }
		}
		showDialog(itemMenuDialog, MENU_TAG)
	}

//	override fun editNotebookName(notebook: NotebookEntity) {
//		val uiState = NotebookUiState(id = notebook.id, name = notebook.name, operation = Operation.Update)
//		val folderDialog = NotebookDialogFragment.newInstance(uiState)
//		showDialog(folderDialog, TAG)
//	}
//
//	override fun deleteNotebook(notebook: Notebook) {
////		viewModel.deleteNotebookAndNotes(notebook)
////		binding.root.showSnackbar(R.string.delete_notes_warning)
//	}

	private fun onBackPressed() {
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			if (viewModel.shouldNavigate) {
				// HACK: If a folder is deleted and the fragment with such folder name
				// was in the back stack, we navigate to "All folders".
				// There is a bug here...
				val args = bundleOf(NOTEBOOK to NotebookEntity())
				findNavController().tryNavigate(R.id.action_notebooks_to_notes, args)
			}
			else {
				findNavController().popBackStack()
			}
		}
	}

	companion object {
		const val NOTEBOOK = "notebook"
		private const val TAG = "NOTEBOOK_DIALOG"
		private const val MENU_TAG = "NOTEBOOK_ITEM_MENU_DIALOG"
	}
}