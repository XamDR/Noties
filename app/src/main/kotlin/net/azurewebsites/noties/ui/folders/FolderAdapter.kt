package net.azurewebsites.noties.ui.folders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FolderItemBinding
import net.azurewebsites.noties.domain.FolderEntity

class FolderAdapter(private val listener: FolderItemContextMenuListener) :
	ListAdapter<FolderEntity, FolderAdapter.FolderViewHolder>(FolderCallback()) {

	inner class FolderViewHolder(private val binding: FolderItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					showContextMenu(bindingAdapterPosition, it)
				}
			}
		}

		fun bind(folder: FolderEntity) {
			binding.apply {
				this.directory = folder
				executePendingBindings()
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
		val binding = FolderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return FolderViewHolder(binding)
	}

	override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
		val directory = getItem(position)
		holder.bind(directory)
	}

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		recyclerView.addItemDecoration(MaterialDividerItemDecoration(
			recyclerView.context,
			MaterialDividerItemDecoration.VERTICAL
		).apply { dividerColor = MaterialColors.getColor(recyclerView, R.attr.colorPrimary) })
	}

	private fun showContextMenu(position: Int, view: View) {
		val context = view.context
		val selectedFolder = getItem(position)
		val items = getContextMenuItems(selectedFolder, context)
		MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(selectedFolder.name)
			.setAdapter(FolderItemContextMenuAdapter(context, R.layout.folder_context_menu_item, items)) { _, which ->
				when (which) {
					0 -> listener.updateFolderName(selectedFolder)
					1 -> listener.lockFolder(selectedFolder)
					2 -> listener.deleteFolder(selectedFolder)
				}
			}
			.setNegativeButton(R.string.cancel_button, null)
			.show()
	}

	private fun getContextMenuItems(selectedFolder: FolderEntity, context: Context): List<FolderContextMenuItem> {
		val items = if (selectedFolder.id == 1) {
			listOf(
				FolderContextMenuItem(R.drawable.ic_edit_folder_name, context.getString(R.string.edit_folder_name)),
				FolderContextMenuItem(R.drawable.ic_lock_folder, context.getString(R.string.lock_folder)),
			)
		}
		else {
			listOf(
				FolderContextMenuItem(R.drawable.ic_edit_folder_name, context.getString(R.string.edit_folder_name)),
				FolderContextMenuItem(R.drawable.ic_lock_folder, context.getString(R.string.lock_folder)),
				FolderContextMenuItem(R.drawable.ic_delete, context.getString(R.string.delete_folder))
			)
		}
		return items
	}

	class FolderCallback : DiffUtil.ItemCallback<FolderEntity>() {

		override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity) = oldItem == newItem
	}
}