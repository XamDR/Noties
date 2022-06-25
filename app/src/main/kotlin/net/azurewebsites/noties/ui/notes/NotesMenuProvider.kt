package net.azurewebsites.noties.ui.notes

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import net.azurewebsites.noties.R

class NotesMenuProvider(private val listener: NotesMenuListener) : MenuProvider {
	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_notes, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
		R.id.sort_notes -> {
			listener.showSortNotesDialog(); true
		}
		else -> false
	}
}