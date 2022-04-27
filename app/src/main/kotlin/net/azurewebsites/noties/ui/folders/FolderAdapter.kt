package net.azurewebsites.noties.ui.folders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.divider.MaterialDividerItemDecoration
import net.azurewebsites.noties.R
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.databinding.FolderItemBinding

class FolderAdapter : ListAdapter<FolderEntity, FolderAdapter.DirectoryViewHolder>(DirectoryCallback()) {

	inner class DirectoryViewHolder(private val binding: FolderItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					onContextMenuCallback.invoke(it, getItem(bindingAdapterPosition))
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

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
		val binding = FolderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return DirectoryViewHolder(binding)
	}

	override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
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

	fun setOnContextMenuListener(callback: (view: View, folder: FolderEntity) -> Unit) {
		onContextMenuCallback = callback
	}

	private var onContextMenuCallback: (view: View, folder: FolderEntity) -> Unit = { _, _ -> }

	class DirectoryCallback : DiffUtil.ItemCallback<FolderEntity>() {

		override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity) = oldItem == newItem
	}
}