package io.github.xamdr.noties.ui.media

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.xamdr.noties.domain.model.Image

class MediaStateAdapter(
	private val fragment: Fragment,
	private val items: MutableList<Image>,
	private val onItemRemoved: (position: Int) -> Unit) : FragmentStateAdapter(fragment) {

	private val itemsId = items.map { it.id.toLong() }.toMutableList()

	override fun getItemCount() = items.size

	override fun createFragment(position: Int): Fragment {
		val item = items[position]
		return MediaPreviewFragment.newInstance(item)
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
			fragment.findNavController().popBackStack()
		}
	}
}