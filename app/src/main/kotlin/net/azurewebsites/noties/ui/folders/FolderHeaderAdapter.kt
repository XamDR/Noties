package net.azurewebsites.noties.ui.folders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.databinding.FolderHeaderItemBinding

class FolderHeaderAdapter(private val listener: NewFolderItemListener) : RecyclerView.Adapter<FolderHeaderAdapter.FolderHeaderViewHolder>() {

	inner class FolderHeaderViewHolder(binding: FolderHeaderItemBinding) : RecyclerView.ViewHolder(binding.root) {
		init {
			binding.addNewFolder.setOnClickListener { listener.showFolderDialog() }
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHeaderViewHolder {
		val binding = FolderHeaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return FolderHeaderViewHolder(binding)
	}

	override fun onBindViewHolder(holder: FolderHeaderViewHolder, position: Int) { }

	override fun getItemCount() = 1
}