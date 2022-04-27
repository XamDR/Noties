package net.azurewebsites.noties.ui.media

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.azurewebsites.noties.domain.ImageEntity

class ViewPagerStateAdapter(activity: FragmentActivity, private val images: List<ImageEntity>) : FragmentStateAdapter(activity) {

	override fun getItemCount() = images.size

	override fun createFragment(position: Int): FullScreenImageFragment {
		val mediaItem = images[position]
		return FullScreenImageFragment.newInstance(mediaItem)
	}
}