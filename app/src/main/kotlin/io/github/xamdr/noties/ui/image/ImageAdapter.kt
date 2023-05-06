package io.github.xamdr.noties.ui.image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ItemImageBinding
import io.github.xamdr.noties.databinding.ItemSingleImageBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.SpanSizeLookupOwner
import io.github.xamdr.noties.ui.helpers.setOnClickListener
import timber.log.Timber

class ImageAdapter(
	private val onItemClicked: (view: View, position: Int) -> Unit) :
	ListAdapter<Image, BaseImageItemViewHolder>(ImageAdapterCallback()), SpanSizeLookupOwner {

	class ImageItemViewHolder(binding: ItemImageBinding) : BaseImageItemViewHolder(binding)

	class SingleImageItemViewHolder(binding: ItemSingleImageBinding) : BaseImageItemViewHolder(binding)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseImageItemViewHolder = when (viewType) {
		SINGLE_IMAGE -> {
			val binding = ItemSingleImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			SingleImageItemViewHolder(binding).apply {
				setOnClickListener { _, position -> onItemClick(itemView, position) }
			}
		}
		MULTIPLE_IMAGES -> {
			val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			ImageItemViewHolder(binding).apply {
				setOnClickListener { _, position -> onItemClick(itemView, position) }
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

	private fun onItemClick(view: View, position: Int) {
		if (position != RecyclerView.NO_POSITION) {
			onItemClicked(view, position)
		}
	}

	private class ImageAdapterCallback : DiffUtil.ItemCallback<Image>() {

		override fun areItemsTheSame(oldItem: Image, newItem: Image) = oldItem.uri == newItem.uri

		override fun areContentsTheSame(oldItem: Image, newItem: Image) = oldItem == newItem
	}

	private companion object {
		private const val SINGLE_IMAGE = R.layout.item_single_image
		private const val MULTIPLE_IMAGES = R.layout.item_image
	}
}