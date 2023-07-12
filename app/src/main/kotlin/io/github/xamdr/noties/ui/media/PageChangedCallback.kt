package io.github.xamdr.noties.ui.media

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class PageChangedCallback(
	private val activity: AppCompatActivity,
	var size: Int) : ViewPager2.OnPageChangeCallback() {

	override fun onPageSelected(position: Int) {
		activity.supportActionBar?.title = "${position + 1}/${size}"
	}
}