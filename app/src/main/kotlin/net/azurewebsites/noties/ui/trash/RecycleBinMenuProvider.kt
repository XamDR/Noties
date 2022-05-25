package net.azurewebsites.noties.ui.trash

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import net.azurewebsites.noties.R

class RecycleBinMenuProvider(private val listener: RecycleBinMenuListener) : MenuProvider {
	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_recycle_bin, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
		R.id.empty_recycle_bin -> {
			listener.showEmptyRecycleBinDialog(); true
		}
		else -> false
	}
}