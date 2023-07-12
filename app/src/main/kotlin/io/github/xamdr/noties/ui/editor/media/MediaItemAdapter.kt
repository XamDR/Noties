package io.github.xamdr.noties.ui.editor.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ItemMediaBinding
import io.github.xamdr.noties.databinding.ItemSingleMediaBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.SpanSizeLookupOwner
import io.github.xamdr.noties.ui.helpers.setOnClickListener
import timber.log.Timber

class MediaItemAdapter(
	private val onItemClicked: (view: View, position: Int) -> Unit) :
	ListAdapter<MediaItem, BaseMediaItemViewHolder>(MediaItemAdapterCallback()), SpanSizeLookupOwner {

	class ImageItemViewHolder(binding: ItemMediaBinding) : BaseMediaItemViewHolder(binding)

	class SingleImageItemViewHolder(binding: ItemSingleMediaBinding) : BaseMediaItemViewHolder(binding)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMediaItemViewHolder = when (viewType) {
		SINGLE_ITEM -> {
			val binding = ItemSingleMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			SingleImageItemViewHolder(binding).apply {
				setOnClickListener { _, position -> onItemClick(itemView, position) }
			}
		}
		MULTIPLE_ITEMS -> {
			val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			ImageItemViewHolder(binding).apply {
				setOnClickListener { _, position -> onItemClick(itemView, position) }
			}
		}
		else -> throw ClassCastException("Unknown view type: $viewType")
	}

	override fun onBindViewHolder(holder: BaseMediaItemViewHolder, position: Int) {
		val item = getItem(position)
		holder.bind(item)
		Timber.d("MediaItem: %s", item)
	}

	override fun getItemViewType(position: Int) =
		if (itemCount.mod(Constants.SPAN_COUNT) != 0 && position == 0) SINGLE_ITEM
		else MULTIPLE_ITEMS

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

	private class MediaItemAdapterCallback : DiffUtil.ItemCallback<MediaItem>() {

		override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem) = oldItem.uri == newItem.uri

		override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem) = oldItem == newItem
	}

	private companion object {
		private const val SINGLE_ITEM = R.layout.item_single_media
		private const val MULTIPLE_ITEMS = R.layout.item_media
	}
}