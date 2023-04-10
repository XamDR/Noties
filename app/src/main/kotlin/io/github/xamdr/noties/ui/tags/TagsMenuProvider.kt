package io.github.xamdr.noties.ui.tags

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import io.github.xamdr.noties.R

class TagsMenuProvider(private val listener: TagToolbarItemListener) : MenuProvider {

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_tags, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
		R.id.add_new_tag -> {
			listener.showCreateTagDialog(); true
		}
 		else -> false
	}
}