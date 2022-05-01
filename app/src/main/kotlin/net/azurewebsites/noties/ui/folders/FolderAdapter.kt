package net.azurewebsites.noties.ui.folders

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
		val selectedFolder = getItem(position)
		MaterialAlertDialogBuilder(view.context, R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(selectedFolder.name)
			.setItems(if (selectedFolder.id == 1) R.array.default_folder_context_menu_options
			else R.array.folder_context_menu_options) { _, which ->
				when (which) {
					0 -> listener.updateFolderName(selectedFolder)
					1 -> listener.lockFolder(selectedFolder)
					2 -> listener.deleteFolder(selectedFolder)
				}
			}
			.setNegativeButton(R.string.cancel_button, null)
			.show()
	}

	class FolderCallback : DiffUtil.ItemCallback<FolderEntity>() {

		override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity) = oldItem == newItem
	}
}