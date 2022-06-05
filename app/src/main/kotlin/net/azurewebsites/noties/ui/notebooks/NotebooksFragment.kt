package net.azurewebsites.noties.ui.notebooks

import android.app.KeyguardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.getSystemService
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
		val folderDialog = NotebookDialogFragment.newInstance(NotebookUiState())
		showDialog(folderDialog, TAG)
	}

	override fun changeNotebookName(notebook: NotebookEntity) {
		val uiState = NotebookUiState(id = notebook.id, name = notebook.name, operation = Operation.Update)
		val folderDialog = NotebookDialogFragment.newInstance(uiState)
		showDialog(folderDialog, TAG)
	}

	override fun deleteNotebook(notebook: Notebook) {
		viewModel.deleteNotebookAndNotes(notebook)
		binding.root.showSnackbar(R.string.delete_notes_warning)
	}

	override fun lockNotebook(notebook: NotebookEntity) {
		if (!notebook.isProtected) {
			val keyguardManager = context?.getSystemService<KeyguardManager>() ?: return

			if (keyguardManager.isDeviceSecure) {
				val updatedFolder = notebook.copy(isProtected = true)
				viewModel.updateNotebook(updatedFolder)
				binding.root.showSnackbar(R.string.lock_confirmation)
			}
			else {
				binding.root.showSnackbar(R.string.no_lock_found)
			}
		}
		else {
			val updatedFolder = notebook.copy(isProtected = false)
			viewModel.updateNotebook(updatedFolder)
			binding.root.showSnackbar(R.string.unlock_confirmation)
		}
	}

	private fun onBackPressed() {
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			if (viewModel.shouldNavigate) {
				// HACK: If a folder is deleted and the fragment with such folder name
				// was in the back stack, we navigate to "All folders".
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
	}
}