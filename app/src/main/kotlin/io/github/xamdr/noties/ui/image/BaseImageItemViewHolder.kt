package io.github.xamdr.noties.ui.image

import android.view.Menu
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xamdr.noties.databinding.ImageItemBinding
import io.github.xamdr.noties.databinding.SingleImageItemBinding
import io.github.xamdr.noties.domain.model.Image

open class BaseImageItemViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.root.setOnCreateContextMenuListener { menu, _, _ ->
			createContextMenu(menu, bindingAdapterPosition)
		}
	}

	open fun createContextMenu(menu: Menu, position: Int) {}

	fun bind(image: Image) {
		when (binding) {
			is ImageItemBinding -> binding.image.setImageURI(image.uri)
			is SingleImageItemBinding -> binding.image.setImageURI(image.uri)
		}
	}
}