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
		else -> false
	}
}