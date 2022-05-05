package net.azurewebsites.noties.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.databinding.FragmentEditorImagesBinding
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.ui.media.MediaItemAdapter

class EditorMediaItemAdapter(
		private val mediaItemAdapter: MediaItemAdapter) : RecyclerView.Adapter<EditorMediaItemAdapter.TextEditorImagesViewHolder>() {

	inner class TextEditorImagesViewHolder(binding: FragmentEditorImagesBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.images.adapter = mediaItemAdapter
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextEditorImagesViewHolder {
		val binding = FragmentEditorImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return TextEditorImagesViewHolder(binding)
	}

	override fun onBindViewHolder(holder: TextEditorImagesViewHolder, position: Int) { }

	override fun getItemCount() = 1

	fun submitList(list: List<ImageEntity>) = mediaItemAdapter.submitList(list)

	fun setOnCopyItemListener(callback: (position: Int) -> Unit) {
		mediaItemAdapter.setOnCopyItemListener(callback)
	}

	fun setOnDeleteItemListener(callback: (position: Int) -> Unit) {
		mediaItemAdapter.setOnDeleteItemListener(callback)
	}

	fun setOnDeleteAllListener(callback: () -> Unit) {
		mediaItemAdapter.setOnDeleteAllListener(callback)
	}

	fun setOnAltTextListener(callback: (position: Int, contentDescription: String) -> Unit) {
		mediaItemAdapter.setOnAltTextListener(callback)
	}
}