package io.github.xamdr.noties.ui.gallery

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.xamdr.noties.domain.model.Image

class ViewPagerStateAdapter(
	activity: FragmentActivity,
	private val images: List<Image>) : FragmentStateAdapter(activity) {

	override fun getItemCount() = images.size

	override fun createFragment(position: Int): FullScreenImageFragment {
		val image = images[position]
		return FullScreenImageFragment.newInstance(image)
	}
}