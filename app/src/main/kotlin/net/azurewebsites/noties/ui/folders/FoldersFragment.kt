package net.azurewebsites.noties.ui.folders

import android.app.KeyguardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentFoldersBinding
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.ui.helpers.showSnackbar

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

	override fun updateFolderName(folder: FolderEntity) {
		val args = bundleOf(FolderDialogFragment.KEY to folder)
		findNavController().navigate(R.id.action_folders_to_folder_dialog, args)
	}

	override fun deleteFolder(folder: FolderEntity) {
		MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(R.string.delete_folder)
			.setMessage(R.string.delete_notes_warning)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ ->
				viewModel.deleteFolders(listOf(folder))
				viewModel.currentFolder.value = FolderEntity()
			}.show()
	}

	override fun lockFolder(folder: FolderEntity) {
		if (!folder.isProtected) {
			val keyguardManager = context?.getSystemService<KeyguardManager>() ?: return

			if (keyguardManager.isDeviceSecure) {
				val updatedFolder = folder.copy(isProtected = true)
				viewModel.upsertFolder(updatedFolder)
				MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
					.setMessage(R.string.lock_confirmation)
					.setPositiveButton(R.string.ok_button, null).show()
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