package io.github.xamdr.noties.ui.image

import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xamdr.noties.databinding.ItemImageBinding
import io.github.xamdr.noties.databinding.ItemSingleImageBinding
import io.github.xamdr.noties.domain.model.Image

open class BaseImageItemViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(image: Image) {
		when (binding) {
			is ItemImageBinding -> ImageLoader.load(binding.image, image.uri, 200)
			is ItemSingleImageBinding -> ImageLoader.load(binding.image, image.uri, 800)
		}
		ViewCompat.setTransitionName(binding.root, image.id.toString())
	}
}