package net.azurewebsites.eznotes.ui.media

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.azurewebsites.eznotes.core.MediaItemEntity

class ViewPagerStateAdapter(activity: FragmentActivity, private val mediaItems: List<MediaItemEntity>) : FragmentStateAdapter(activity) {

	override fun getItemCount() = mediaItems.size

	override fun createFragment(position: Int): FullScreenImageFragment {
		val mediaItem = mediaItems[position]
		return FullScreenImageFragment.newInstance(mediaItem)
	}
}