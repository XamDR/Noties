package net.azurewebsites.noties.ui.folders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import net.azurewebsites.noties.R

data class FolderContextMenuItem(val icon: Int, val text: String)

class FolderItemContextMenuAdapter(
	context: Context,
	@LayoutRes private val resource: Int,
	private val items: List<FolderContextMenuItem>) : ArrayAdapter<FolderContextMenuItem>(context, resource, items) {

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val layoutInflater = LayoutInflater.from(context)
		return convertView ?: layoutInflater.inflate(resource, parent, false).apply {
			findViewById<TextView>(R.id.item).run {
				text = items[position].text
				setCompoundDrawablesRelativeWithIntrinsicBounds(items[position].icon, 0, 0, 0)
			}
		}
	}
}