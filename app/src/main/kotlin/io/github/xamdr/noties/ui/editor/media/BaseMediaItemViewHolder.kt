package io.github.xamdr.noties.ui.editor.media

import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.ItemMediaBinding
import io.github.xamdr.noties.databinding.ItemSingleMediaBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.media.ImageLoader
import io.github.xamdr.noties.ui.helpers.media.MediaHelper

open class BaseMediaItemViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(item: MediaItem) {
		when (binding) {
			is ItemMediaBinding -> {
				when (item.mediaType) {
					MediaType.Image -> {
						binding.duration.isVisible = false
						ImageLoader.load(binding.image, item.uri, 800)
					}
					MediaType.Video -> {
						binding.duration.isVisible = true
						binding.duration.text = MediaHelper.formatDuration(item.metadata.duration)
						ImageLoader.load(binding.image, item.metadata.thumbnail, 800)
					}
					MediaType.Audio -> {}
				}
			}
			is ItemSingleMediaBinding -> {
				when (item.mediaType) {
					MediaType.Image -> {
						binding.duration.isVisible = false
						ImageLoader.load(binding.image, item.uri, 800)
					}
					MediaType.Video -> {
						binding.duration.isVisible = true
						binding.duration.text = MediaHelper.formatDuration(item.metadata.duration)
						ImageLoader.load(binding.image, item.metadata.thumbnail, 800)
					}
					MediaType.Audio -> {}
				}
			}
		}
		ViewCompat.setTransitionName(binding.root, item.id.toString())
	}
}