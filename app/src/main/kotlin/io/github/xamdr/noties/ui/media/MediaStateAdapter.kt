package io.github.xamdr.noties.ui.media

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.onBackPressed

class MediaStateAdapter(
	private val fragment: Fragment,
	private val items: MutableList<MediaItem>,
	private val onItemRemoved: (position: Int) -> Unit) : FragmentStateAdapter(fragment) {

	private val itemsId = items.map { it.id.toLong() }.toMutableList()

	override fun getItemCount() = items.size

	override fun createFragment(position: Int): Fragment {
		val item = items[position]
		return when (item.mediaType) {
			MediaType.Image -> ImageMediaPreviewFragment.newInstance(item)
			MediaType.Video -> VideoMediaPreviewFragment.newInstance(item)
			MediaType.Audio -> throw IllegalArgumentException("Wrong mediatype: ${item.mediaType}")
		}
	}

	override fun getItemId(position: Int) = items[position].id.toLong()

	override fun containsItem(itemId: Long) = itemsId.contains(itemId)

	fun findFragmentByPosition(position: Int): Fragment? {
		val tag = "f${getItemId(position)}"
		return fragment.childFragmentManager.findFragmentByTag(tag)
	}

	fun removeFragment(position: Int) {
		onItemRemoved(position)
		items.removeAt(position)
		itemsId.removeAt(position)
		notifyItemRemoved(position)
		if (itemCount == 0) {
			fragment.onBackPressed()
		}
	}
}