package io.github.xamdr.noties.ui.notes

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.settings.PreferenceStorage

class NotesMenuProvider(
	private val listener: NotesMenuListener,
	private val preferenceStorage: PreferenceStorage) : MenuProvider {

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.menu_notes, menu)
		onPrepareMenu(menu) // We shouldn't need to call this method here (bug?)
	}

	override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
		R.id.change_notes_layout -> {
			listener.changeNotesLayout(LayoutType.valueOf(preferenceStorage.layoutType)); true
		}
		else -> false
	}

	override fun onPrepareMenu(menu: Menu) {
		super.onPrepareMenu(menu)
		val item = menu.findItem(R.id.change_notes_layout)
		val layoutType = LayoutType.valueOf(preferenceStorage.layoutType)
		item.setIcon(when (layoutType) {
			LayoutType.Linear -> R.drawable.ic_view_grid_layout
			LayoutType.Grid -> R.drawable.ic_view_linear_layout
		})
		item.setTitle(when (layoutType) {
			LayoutType.Linear -> R.string.set_grid_layout
			LayoutType.Grid -> R.string.set_linear_layout
		})
	}
}