package net.azurewebsites.noties.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.databinding.FragmentEditorImagesBinding
import net.azurewebsites.noties.ui.image.ImageAdapter

class EditorImageAdapter(
		private val imageAdapter: ImageAdapter
) : RecyclerView.Adapter<EditorImageAdapter.TextEditorImagesViewHolder>() {

	inner class TextEditorImagesViewHolder(binding: FragmentEditorImagesBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.images.adapter = imageAdapter
			(binding.images.layoutManager as GridLayoutManager).spanSizeLookup =
				object : GridLayoutManager.SpanSizeLookup() {
					// We set the first image full size if the number of images is odd,
					// otherwise we just leave the layout as it is.
					override fun getSpanSize(position: Int) =
						if (imageAdapter.itemCount.mod(2) != 0 && position == 0) SPAN_COUNT else 1
				}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextEditorImagesViewHolder {
		val binding = FragmentEditorImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return TextEditorImagesViewHolder(binding)
	}

	override fun onBindViewHolder(holder: TextEditorImagesViewHolder, position: Int) { }

	override fun getItemCount() = 1

	fun submitList(list: List<ImageEntity>) = imageAdapter.submitList(list)

	private companion object {
		private const val SPAN_COUNT = 2
	}
}