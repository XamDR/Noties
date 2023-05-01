package io.github.xamdr.noties.ui.image

import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ImageItemBinding
import io.github.xamdr.noties.databinding.SingleImageItemBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.SpanSizeLookupOwner
import io.github.xamdr.noties.ui.helpers.setOnClickListener
import timber.log.Timber

class ImageAdapter(
	private val onImageClicked: (images: List<Image>, position: Int) -> Unit,
	private val listener: ImageItemContextMenuListener) :
	ListAdapter<Image, BaseImageItemViewHolder>(ImageAdapterCallback()), SpanSizeLookupOwner {

	inner class ImageItemViewHolder(binding: ImageItemBinding) : BaseImageItemViewHolder(binding) {

		override fun createContextMenu(menu: Menu, position: Int) {
			showContextMenu(menu, position)
		}
	}

	inner class SingleImageItemViewHolder(binding: SingleImageItemBinding) : BaseImageItemViewHolder(binding) {

		override fun createContextMenu(menu: Menu, position: Int) {
			showContextMenu(menu, position)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseImageItemViewHolder = when (viewType) {
		SINGLE_IMAGE -> {
			val binding = SingleImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			SingleImageItemViewHolder(binding).apply {
				setOnClickListener { _, position -> onItemClick(currentList, position) }
			}
		}
		MULTIPLE_IMAGES -> {
			val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			ImageItemViewHolder(binding).apply {
				setOnClickListener { _, position -> onItemClick(currentList, position) }
			}
		}
		else -> throw ClassCastException("Unknown view type: $viewType")
	}

	override fun onBindViewHolder(holder: BaseImageItemViewHolder, position: Int) {
		val image = getItem(position)
		holder.bind(image)
		Timber.d("Image: %s", image)
	}

	override fun getItemViewType(position: Int) =
		if (itemCount.mod(Constants.SPAN_COUNT) != 0 && position == 0) SINGLE_IMAGE
		else MULTIPLE_IMAGES

	override fun getSpanSizeLookup() = object : GridLayoutManager.SpanSizeLookup() {
		override fun getSpanSize(position: Int) =
			if (itemCount.mod(Constants.SPAN_COUNT) != 0 && position == 0) Constants.SPAN_COUNT
			else 1
	}

	private fun onItemClick(images: List<Image>, position: Int) {
		if (position != RecyclerView.NO_POSITION) {
			onImageClicked(images, position)
		}
	}

	private fun showContextMenu(menu: Menu, position: Int) {
		val image = getItem(position)
		menu.run {
			add(R.string.copy_item).setOnMenuItemClickListener {
				listener.copyImage(position); true
			}
			if (image.id != 0) {
				add(if (image.description.isNullOrEmpty()) R.string.add_alt_text
					else R.string.update_alt_text).setOnMenuItemClickListener {
						listener.addAltText(position); true
					}
			}
			add(R.string.delete_item).setOnMenuItemClickListener {
				listener.deleteImage(position); true
			}
		}
	}

	private class ImageAdapterCallback : DiffUtil.ItemCallback<Image>() {

		override fun areItemsTheSame(oldItem: Image, newItem: Image) = oldItem.uri == newItem.uri

		override fun areContentsTheSame(oldItem: Image, newItem: Image) = oldItem == newItem
	}

	private companion object {
		private const val SINGLE_IMAGE = R.layout.single_image_item
		private const val MULTIPLE_IMAGES = R.layout.image_item
	}
}