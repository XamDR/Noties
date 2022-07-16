package net.azurewebsites.noties.ui.editor

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import net.azurewebsites.noties.R

class MenuItemClickListener(private val listener: ToolbarItemMenuListener) : Toolbar.OnMenuItemClickListener {
	override fun onMenuItemClick(item: MenuItem) = when (item.itemId) {
		R.id.share_content -> {
			listener.shareContent(); true
		}
		R.id.delete_images -> {
			listener.showDeleteImagesDialog(); true
		}
		R.id.open_file -> {
			listener.openTextFile(); true
		}
		R.id.hide_todos -> {
			listener.hideTodoList(); true
		}
		else -> false
	}
}