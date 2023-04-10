package io.github.xamdr.noties.ui.notebooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.NotebookEntityCrossRefLocal
import io.github.xamdr.noties.data.entity.NotebookEntityLocal
import io.github.xamdr.noties.databinding.FragmentNotebooksBinding
import io.github.xamdr.noties.ui.helpers.addMenuProvider
import io.github.xamdr.noties.ui.helpers.showDialog
import io.github.xamdr.noties.ui.helpers.showSnackbar
import io.github.xamdr.noties.ui.helpers.tryNavigate

@AndroidEntryPoint
class NotebooksFragment : Fragment(), NotebookToolbarItemListener, NotebookItemPopupMenuListener {

	private var _binding: FragmentNotebooksBinding? = null
	private val binding get() = _binding!!
	private val viewModel by activityViewModels<NotebooksViewModel>()
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

	override fun showEditNotebookNameDialog(notebook: NotebookEntityLocal) {
		val uiState = NotebookUiState(id = notebook.id, name = notebook.name, operation = Operation.Update)
		val notebookDialog = NotebookDialogFragment.newInstance(uiState)
		showDialog(notebookDialog, TAG)
	}

	override fun deleteNotebook(notebook: NotebookEntityCrossRefLocal) {
		viewModel.deleteNotebookAndNotes(notebook) {
			binding.root.showSnackbar(R.string.delete_notes_message)
		}
	}

	private fun onBackPressed() {
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			if (viewModel.shouldNavigate) {
				// HACK: If a folder is deleted and the fragment with such folder name
				// was in the back stack, we navigate to "All folders".
				// There is a bug here...
				val args = bundleOf(NOTEBOOK to NotebookEntityLocal())
				findNavController().tryNavigate(R.id.action_notebooks_to_notes, args)
			}
			else {
				findNavController().popBackStack()
			}
		}
	}

	companion object {
		const val NOTEBOOK = "notebook"
		const val TAG = "NOTEBOOK_DIALOG"
	}
}