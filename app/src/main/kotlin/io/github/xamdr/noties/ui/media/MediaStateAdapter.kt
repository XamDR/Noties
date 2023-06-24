package io.github.xamdr.noties.ui.media

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.MediaItem

class MediaStateAdapter(
	private val activity: FragmentActivity,
	private val items: MutableList<MediaItem>,
	private val onItemRemoved: (position: Int) -> Unit) : FragmentStateAdapter(activity) {

	private val itemsId = items.map { it.id.toLong() }.toMutableList()

	override fun getItemCount() = items.size

	override fun createFragment(position: Int): Fragment {
		val item = items[position]
		return when (item.mediaType) {
			MediaType.Image -> ImageMediaViewerFragment.newInstance(item)
			MediaType.Video -> VideoMediaViewerFragment.newInstance(item)
			MediaType.Audio -> throw IllegalArgumentException("Wrong mediatype: ${item.mediaType}")
		}
	}

	override fun getItemId(position: Int) = items[position].id.toLong()

	override fun containsItem(itemId: Long) = itemsId.contains(itemId)

	fun findFragmentByPosition(position: Int): Fragment? {
		val tag = "f${getItemId(position)}"
		return activity.supportFragmentManager.findFragmentByTag(tag)
	}

	fun removeFragment(position: Int) {
		onItemRemoved(position)
		items.removeAt(position)
		itemsId.removeAt(position)
		notifyItemRemoved(position)
		if (itemCount == 0) {
			activity.finish()
		}
	}
}