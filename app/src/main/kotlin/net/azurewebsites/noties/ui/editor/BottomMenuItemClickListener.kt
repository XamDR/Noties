package net.azurewebsites.noties.ui.editor

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import net.azurewebsites.noties.R

class BottomMenuItemClickListener(private val listener: BottomToolbarMenuListener): Toolbar.OnMenuItemClickListener {

	override fun onMenuItemClick(item: MenuItem) = when (item.itemId) {
		R.id.add -> {
			listener.showBottomSheetMenuDialog(); true
		}
		R.id.note_color -> {
			listener.showBottomSheetColorDialog(); true
		}
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