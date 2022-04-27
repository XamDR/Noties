package net.azurewebsites.eznotes.ui.folders

import android.app.KeyguardManager
import android.os.Bundle
import android.view.*
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.core.DirectoryEntity
import net.azurewebsites.eznotes.databinding.FragmentFolderListBinding
import net.azurewebsites.eznotes.ui.helpers.OnFabClickListener
import net.azurewebsites.eznotes.ui.helpers.mainActivity
import net.azurewebsites.eznotes.ui.helpers.showSnackbar

class FolderListFragment : Fragment(), OnFabClickListener {

	private var _binding: FragmentFolderListBinding? = null
	private val binding get() = _binding!!
	private val viewModel by activityViewModels<FolderListViewModel>()
	private val folderAdapter = FolderAdapter()
	private lateinit var directory: DirectoryEntity

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentFolderListBinding.inflate(inflater, container, false)
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
		menu.setHeaderTitle(directory.name)
		mainActivity.menuInflater.inflate(R.menu.menu_folder_item, menu)
		if (directory.id == 1) {
			menu.findItem(R.id.delete_folder).apply { isVisible = false }
		}
		if (directory.isProtected) {
			menu.findItem(R.id.lock_folder).apply { title = getString(R.string.unlock_folder) }
		}
	}

	override fun onContextItemSelected(item: MenuItem) = when (item.itemId) {
		R.id.edit_folder_name -> {
			showFolderDialog(directory); true
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
		mainActivity.setOnFabClickListener { onClick() }
		folderAdapter.setOnContextMenuListener { view, directory ->
			this.directory = directory
			view.showContextMenu()
		}
	}

	override fun onClick() = showFolderDialog(DirectoryEntity())

	private fun showFolderDialog(directory: DirectoryEntity) {
		val folderDialog = FolderDialogFragment.newInstance(directory)
		folderDialog.show(parentFragmentManager, TAG)
	}

	private fun showDeletionFolderWarningDialog() {
		MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(R.string.delete_folder)
			.setMessage(R.string.delete_notes_warning)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ ->
				viewModel.deleteDirectories(listOf(directory))
				viewModel.currentDirectory.value = DirectoryEntity()
			}.show()
	}

	private fun lockFolder() {
		if (!directory.isProtected) {
			val keyguardManager = context?.getSystemService<KeyguardManager>() ?: return

			if (keyguardManager.isDeviceSecure) {
				val updatedDirectory = directory.copy(isProtected = true)
				viewModel.upsertDirectory(updatedDirectory)
				MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
					.setMessage(R.string.lock_confirmation)
					.setPositiveButton(R.string.ok_button, null).show()
			}
			else {
				binding.root.showSnackbar(R.string.no_lock_found)
			}
		}
		else {
			val updatedDirectory = directory.copy(isProtected = false)
			viewModel.upsertDirectory(updatedDirectory)
			binding.root.showSnackbar(R.string.unlock_confirmation)
		}
	}

	companion object {
		private const val TAG = "FOLDER_DIALOG"
	}
}