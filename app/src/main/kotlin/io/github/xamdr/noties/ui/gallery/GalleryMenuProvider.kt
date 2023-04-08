package io.github.xamdr.noties.ui.gallery

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.image.BitmapCache

class GalleryMenuProvider(private val listener: GalleryMenuListener) : MenuProvider {

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_image_full_screen, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
		R.id.share_image -> {
			listener.shareImage(listener.currentItem); true
		}
		R.id.set_as -> {
			listener.setImageAs(listener.currentItem); true
		}
		R.id.print_image -> {
			listener.printImage(listener.currentItem); true
		}
		android.R.id.home -> {
			BitmapCache.Instance.clear()
			listener.onBackPressed(); true
		}
		else -> false
	}
}