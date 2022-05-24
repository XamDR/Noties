package net.azurewebsites.noties.ui.notes

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import net.azurewebsites.noties.R

class NotesMenuProvider : MenuProvider {
	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_notes, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		TODO("Not yet implemented")
	}
}