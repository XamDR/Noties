package net.azurewebsites.noties.ui.folders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.FolderItemBinding
import net.azurewebsites.noties.ui.helpers.setOnClickListener
import net.azurewebsites.noties.ui.helpers.setOnSingleClickListener
import net.azurewebsites.noties.ui.helpers.tryNavigate

class FolderAdapter(private val listener: FolderItemContextMenuListener) :
	ListAdapter<Folder, FolderAdapter.FolderViewHolder>(FolderCallback()) {

	inner class FolderViewHolder(private val binding: FolderItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnSingleClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					showContextMenu(bindingAdapterPosition, it)
				}
			}
		}

		fun bind(folder: FolderEntity) {
			binding.apply {
				this.folder = folder
				executePendingBindings()
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
		val binding = FolderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return FolderViewHolder(binding).apply {
			setOnClickListener { position, _ -> navigateToNotes(this, position) }
		}
	}

	override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
		val folder = getItem(position)
		holder.bind(folder.entity)
	}

	private fun navigateToNotes(holder: RecyclerView.ViewHolder, position: Int) {
		val folder = getItem(position).entity
		val args = bundleOf(FoldersFragment.FOLDER to folder)
		holder.itemView.findNavController().tryNavigate(R.id.action_folders_to_notes, args)
	}

	private fun showContextMenu(position: Int, view: View) {
		val context = view.context
		val selectedFolder = getItem(position)
		val items = getContextMenuItems(selectedFolder, context)
		MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(selectedFolder.entity.name)
			.setAdapter(FolderItemContextMenuAdapter(context, R.layout.folder_context_menu_item, items)) { _, which ->
				when (which) {
					0 -> listener.updateFolderName(selectedFolder.entity)
					1 -> listener.lockFolder(selectedFolder.entity)
					2 -> listener.deleteFolder(selectedFolder)
				}
			}
			.setNegativeButton(R.string.cancel_button, null)
			.show()
			.window?.setWindowAnimations(R.style.ScaleAnimationDialog)
	}

	private fun getContextMenuItems(selectedFolder: Folder, context: Context): List<FolderContextMenuItem> {
		val items = if (selectedFolder.entity.id == 1) {
			listOf(
				FolderContextMenuItem(R.drawable.ic_edit_folder_name, context.getString(R.string.update_folder_name)),
				FolderContextMenuItem(
					if (selectedFolder.entity.isProtected) R.drawable.ic_unlock_folder else R.drawable.ic_lock_folder,
					if (selectedFolder.entity.isProtected) context.getString(R.string.unlock_folder)
					else context.getString(R.string.lock_folder)
				),
			)
		}
		else {
			listOf(
				FolderContextMenuItem(R.drawable.ic_edit_folder_name, context.getString(R.string.update_folder_name)),
				FolderContextMenuItem(
					if (selectedFolder.entity.isProtected) R.drawable.ic_unlock_folder else R.drawable.ic_lock_folder,
					if (selectedFolder.entity.isProtected) context.getString(R.string.unlock_folder)
					else context.getString(R.string.lock_folder)
				),
				FolderContextMenuItem(R.drawable.ic_delete, context.getString(R.string.delete_folder))
			)
		}
		return items
	}

	class FolderCallback : DiffUtil.ItemCallback<Folder>() {

		override fun areItemsTheSame(oldItem: Folder, newItem: Folder) = oldItem.entity.id == newItem.entity.id

		override fun areContentsTheSame(oldItem: Folder, newItem: Folder) = oldItem == newItem
	}
}