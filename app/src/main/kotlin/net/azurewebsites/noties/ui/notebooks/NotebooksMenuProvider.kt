package net.azurewebsites.noties.ui.notebooks

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import net.azurewebsites.noties.R

class NotebooksMenuProvider(private val listener: NotebookToolbarItemListener) : MenuProvider {
	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_notebooks, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
		R.id.add_new_notebook -> {
			listener.showCreateNotebookDialog(); true
		}
 		else -> false
	}
}