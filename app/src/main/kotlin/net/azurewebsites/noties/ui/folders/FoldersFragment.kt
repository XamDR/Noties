package net.azurewebsites.noties.ui.folders

import android.app.KeyguardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.FragmentFoldersBinding
import net.azurewebsites.noties.ui.helpers.printDebug
import net.azurewebsites.noties.ui.helpers.showSnackbar

@AndroidEntryPoint
class FoldersFragment : Fragment(), NewFolderItemListener, FolderItemContextMenuListener {

	private var _binding: FragmentFoldersBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<FoldersViewModel>()
	private val headerAdapter = FolderHeaderAdapter(this)
	private val folderAdapter = FolderAdapter(this)

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentFoldersBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.folders.adapter = ConcatAdapter(headerAdapter, folderAdapter)
		viewModel.folders.observe(viewLifecycleOwner) { folderAdapter.submitList(it) }
	}

	override fun showFolderDialog() {
		val previousDialog = childFragmentManager.findFragmentByTag(TAG)
		// We check if the dialog exists in order to prevent to show it twice if the user clicks too fast
		if (previousDialog == null) {
			val folderDialog = FolderDialogFragment.newInstance(FolderUiState())
			folderDialog.show(childFragmentManager, TAG)
		}
	}

	override fun updateFolderName(folder: FolderEntity) {
		val uiState = FolderUiState(id = folder.id, name = folder.name, operation = Operation.Update)
		val folderDialog = FolderDialogFragment.newInstance(uiState)
		folderDialog.show(childFragmentManager, TAG)
	}

	override fun deleteFolder(folder: Folder) {
		viewModel.deleteFolderAndNotes(folder)
		viewModel.updateCurrentFolder(FolderEntity())
		printDebug("SELECTED_FOLDER", viewModel.selectedFolder.value)
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

	private companion object {
		private const val TAG = "FOLDER_DIALOG"
	}
}