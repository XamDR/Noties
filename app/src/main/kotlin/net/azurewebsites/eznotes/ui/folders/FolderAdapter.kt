package net.azurewebsites.eznotes.ui.folders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.divider.MaterialDividerItemDecoration
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.core.DirectoryEntity
import net.azurewebsites.eznotes.databinding.FolderItemBinding

class FolderAdapter : ListAdapter<DirectoryEntity, FolderAdapter.DirectoryViewHolder>(DirectoryCallback()) {

	inner class DirectoryViewHolder(private val binding: FolderItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					onContextMenuCallback.invoke(it, getItem(bindingAdapterPosition))
				}
			}
		}

		fun bind(directory: DirectoryEntity) {
			binding.apply {
				this.directory = directory
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

	fun setOnContextMenuListener(callback: (view: View, directory: DirectoryEntity) -> Unit) {
		onContextMenuCallback = callback
	}

	private var onContextMenuCallback: (view: View, directory: DirectoryEntity) -> Unit = { _, _ -> }

	class DirectoryCallback : DiffUtil.ItemCallback<DirectoryEntity>() {

		override fun areItemsTheSame(oldItem: DirectoryEntity, newItem: DirectoryEntity) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: DirectoryEntity, newItem: DirectoryEntity) = oldItem == newItem
	}
}