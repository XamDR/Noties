package net.azurewebsites.noties.ui.gallery

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.azurewebsites.noties.core.ImageEntity

class ViewPagerStateAdapter(
	activity: FragmentActivity,
	private val images: List<ImageEntity>) : FragmentStateAdapter(activity) {

	override fun getItemCount() = images.size

	override fun createFragment(position: Int): FullScreenImageFragment {
		val image = images[position]
		return FullScreenImageFragment.newInstance(image)
	}
}