package net.azurewebsites.noties.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.databinding.FragmentEditorImagesBinding
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.ui.media.ImageAdapter

class EditorImageAdapter(
		private val imageAdapter: ImageAdapter) : RecyclerView.Adapter<EditorImageAdapter.TextEditorImagesViewHolder>() {

	inner class TextEditorImagesViewHolder(binding: FragmentEditorImagesBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.images.adapter = imageAdapter
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextEditorImagesViewHolder {
		val binding = FragmentEditorImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return TextEditorImagesViewHolder(binding)
	}

	override fun onBindViewHolder(holder: TextEditorImagesViewHolder, position: Int) { }

	override fun getItemCount() = 1

	fun submitList(list: List<ImageEntity>) = imageAdapter.submitList(list)
}