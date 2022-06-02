package net.azurewebsites.noties.ui.folders

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.FragmentFoldersBinding
import net.azurewebsites.noties.ui.MainActivity
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.notes.NotesFragment

@AndroidEntryPoint
class FoldersFragment : Fragment(), FolderToolbarItemListener, FolderItemContextMenuListener,
	NavigateToEditorListener {

	private var _binding: FragmentFoldersBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<FoldersViewModel>()
	private val folderAdapter = FolderAdapter(this)
	private val menuProvider = FoldersMenuProvider(this)

	override fun onAttach(context: Context) {
		super.onAttach(context)
		(context as MainActivity).setOnFabClickListener { navigateToEditor() }
	}

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentFoldersBinding.inflate(inflater, container, false)
		addMenuProvider(menuProvider, viewLifecycleOwner)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.folders.adapter = folderAdapter
		viewModel.folders.observe(viewLifecycleOwner) { folderAdapter.submitList(it) }
	}

	override fun showFolderDialog() {
		val folderDialog = FolderDialogFragment.newInstance(FolderUiState())
		showDialog(folderDialog, TAG)
	}

	override fun navigateToSettings() =
		findNavController().tryNavigate(R.id.action_folders_to_settings)

	override fun updateFolderName(folder: FolderEntity) {
		val uiState = FolderUiState(id = folder.id, name = folder.name, operation = Operation.Update)
		val folderDialog = FolderDialogFragment.newInstance(uiState)
		showDialog(folderDialog, TAG)
	}

	override fun deleteFolder(folder: Folder) {
		viewModel.deleteFolderAndNotes(folder)
		binding.root.showSnackbar(R.string.delete_notes_warning)
	}

	override fun lockFolder(folder: FolderEntity) {
		if (!folder.isProtected) {
			val keyguardManager = context?.getSystemService<KeyguardManager>() ?: return

			if (keyguardManager.isDeviceSecure) {
				val updatedFolder = folder.copy(isProtected = true)
				viewModel.updateFolder(updatedFolder)
				binding.root.showSnackbar(R.string.lock_confirmation)
			}
			else {
				binding.root.showSnackbar(R.string.no_lock_found)
			}
		}
		else {
			val updatedFolder = folder.copy(isProtected = false)
			viewModel.updateFolder(updatedFolder)
			binding.root.showSnackbar(R.string.unlock_confirmation)
		}
	}

	override fun navigateToEditor() {
		val args = bundleOf(NotesFragment.ID to 1)
		findNavController().tryNavigate(R.id.action_folders_to_editor, args)
	}

	companion object {
		const val FOLDER = "folder"
		private const val TAG = "FOLDER_DIALOG"
	}
}