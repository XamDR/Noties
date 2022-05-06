package net.azurewebsites.noties.ui.folders

import android.app.KeyguardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.FragmentFoldersBinding
import net.azurewebsites.noties.ui.helpers.printDebug
import net.azurewebsites.noties.ui.helpers.showSnackbar
import net.azurewebsites.noties.ui.helpers.tryNavigate

@AndroidEntryPoint
class FoldersFragment : Fragment(), FolderItemContextMenuListener {

	private var _binding: FragmentFoldersBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<FoldersViewModel>()
	private val folderAdapter = FolderAdapter(this)
	private val headerAdapter = FolderHeaderAdapter()

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

	override fun onStart() {
		super.onStart()
		setFragmentResultListener("result") { _, bundle ->
			if (bundle.getBoolean("result")) {
				folderAdapter.notifyItemChanged(viewModel.position)
			}
		}
	}

	override fun updateFolderName(folder: FolderEntity, position: Int) {
		viewModel.position = position
		printDebug("POS", viewModel.position)
		val args = bundleOf(FolderDialogFragment.KEY to folder)
		findNavController().tryNavigate(R.id.action_folders_to_folder_dialog, args)
	}

	override fun deleteFolder(folder: Folder) {
		viewModel.deleteFolderAndNotes(folder)
		viewModel.currentFolder.value = FolderEntity()
		binding.root.showSnackbar(R.string.delete_notes_warning)
	}

	override fun lockFolder(folder: FolderEntity) {
		if (!folder.isProtected) {
			val keyguardManager = context?.getSystemService<KeyguardManager>() ?: return

			if (keyguardManager.isDeviceSecure) {
				val updatedFolder = folder.copy(isProtected = true)
				viewModel.upsertFolder(updatedFolder)
				binding.root.showSnackbar(R.string.lock_confirmation)
			}
			else {
				binding.root.showSnackbar(R.string.no_lock_found)
			}
		}
		else {
			val updatedFolder = folder.copy(isProtected = false)
			viewModel.upsertFolder(updatedFolder)
			binding.root.showSnackbar(R.string.unlock_confirmation)
		}
	}
}