package net.azurewebsites.noties.ui.folders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FolderHeaderItemBinding
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.ui.helpers.tryNavigate

class FolderHeaderAdapter : RecyclerView.Adapter<FolderHeaderAdapter.FolderHeaderViewHolder>() {

	inner class FolderHeaderViewHolder(binding: FolderHeaderItemBinding) : RecyclerView.ViewHolder(binding.root) {
		init {
			binding.addNewFolder.setOnClickListener {
				val args = bundleOf(FolderDialogFragment.KEY to FolderEntity())
				it.findNavController().tryNavigate(R.id.action_folders_to_folder_dialog, args)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHeaderViewHolder {
		val binding = FolderHeaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return FolderHeaderViewHolder(binding)
	}

	override fun onBindViewHolder(holder: FolderHeaderViewHolder, position: Int) { }

	override fun getItemCount() = 1
}