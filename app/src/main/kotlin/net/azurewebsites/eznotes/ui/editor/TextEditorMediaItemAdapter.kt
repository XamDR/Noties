package net.azurewebsites.eznotes.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.databinding.FragmentTextEditorImagesBinding
import net.azurewebsites.eznotes.ui.media.MediaItemAdapter

class TextEditorMediaItemAdapter(
		private val mediaItemAdapter: MediaItemAdapter) : RecyclerView.Adapter<TextEditorMediaItemAdapter.TextEditorImagesViewHolder>() {

	inner class TextEditorImagesViewHolder(
		binding: FragmentTextEditorImagesBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.images.adapter = mediaItemAdapter
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextEditorImagesViewHolder {
		val binding = FragmentTextEditorImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return TextEditorImagesViewHolder(binding)
	}

	override fun onBindViewHolder(holder: TextEditorImagesViewHolder, position: Int) { }

	override fun getItemCount() = 1

	fun submitList(list: List<MediaItemEntity>) = mediaItemAdapter.submitList(list)

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