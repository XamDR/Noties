package net.azurewebsites.noties.ui.folders

import android.app.KeyguardManager
import android.os.Bundle
import android.view.*
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.noties.R
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.databinding.FragmentFoldersBinding
import net.azurewebsites.noties.ui.helpers.mainActivity
import net.azurewebsites.noties.ui.helpers.showSnackbar

class FoldersFragment : Fragment() {

	private var _binding: FragmentFoldersBinding? = null
	private val binding get() = _binding!!
	private val viewModel by activityViewModels<FoldersViewModel>()
	private val folderAdapter = FolderAdapter()
	private lateinit var folder: FolderEntity

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
		binding.folders.adapter = folderAdapter
		registerForContextMenu(binding.folders)
		viewModel.directories.observe(viewLifecycleOwner) { folderAdapter.submitList(it) }
	}

	override fun onCreateContextMenu(menu: ContextMenu,
	                                 v: View,
	                                 menuInfo: ContextMenu.ContextMenuInfo?
	) {
		menu.setHeaderTitle(folder.name)
		mainActivity.menuInflater.inflate(R.menu.menu_folder_item, menu)
		if (folder.id == 1) {
			menu.findItem(R.id.delete_folder).apply { isVisible = false }
		}
		if (folder.isProtected) {
			menu.findItem(R.id.lock_folder).apply { title = getString(R.string.unlock_folder) }
		}
	}

	override fun onContextItemSelected(item: MenuItem) = when (item.itemId) {
		R.id.edit_folder_name -> {
			showFolderDialog(folder); true
		}
		R.id.delete_folder -> {
			showDeletionFolderWarningDialog(); true
		}
		R.id.lock_folder -> {
			lockFolder(); true
		}
		else -> false
	}

	override fun onStart() {
		super.onStart()
		folderAdapter.setOnContextMenuListener { view, directory ->
			this.folder = directory
			view.showContextMenu()
		}
	}

	private fun showFolderDialog(folder: FolderEntity) {
		val folderDialog = FolderDialogFragment.newInstance(folder)
		folderDialog.show(parentFragmentManager, TAG)
	}

	private fun showDeletionFolderWarningDialog() {
		MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(R.string.delete_folder)
			.setMessage(R.string.delete_notes_warning)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ ->
				viewModel.deleteDirectories(listOf(folder))
				viewModel.currentDirectory.value = FolderEntity()
			}.show()
	}

	private fun lockFolder() {
		if (!folder.isProtected) {
			val keyguardManager = context?.getSystemService<KeyguardManager>() ?: return

			if (keyguardManager.isDeviceSecure) {
				val updatedDirectory = folder.copy(isProtected = true)
				viewModel.upsertFolder(updatedDirectory)
				MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
					.setMessage(R.string.lock_confirmation)
					.setPositiveButton(R.string.ok_button, null).show()
			}
			else {
				binding.root.showSnackbar(R.string.no_lock_found)
			}
		}
		else {
			val updatedDirectory = folder.copy(isProtected = false)
			viewModel.upsertFolder(updatedDirectory)
			binding.root.showSnackbar(R.string.unlock_confirmation)
		}
	}

	companion object {
		private const val TAG = "FOLDER_DIALOG"
	}
}