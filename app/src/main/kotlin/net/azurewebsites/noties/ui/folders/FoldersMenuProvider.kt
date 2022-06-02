package net.azurewebsites.noties.ui.folders

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import net.azurewebsites.noties.R

class FoldersMenuProvider(private val listener: FolderToolbarItemListener) : MenuProvider {
	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_folders, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
		R.id.add_new_folder -> {
			listener.showFolderDialog(); true
		}
		R.id.nav_settings -> {
			listener.navigateToSettings(); true
		}
 		else -> false
	}
}