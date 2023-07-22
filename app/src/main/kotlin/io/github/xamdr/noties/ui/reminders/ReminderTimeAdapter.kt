package io.github.xamdr.noties.ui.reminders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import io.github.xamdr.noties.R

class ReminderTimeAdapter(
	context: Context,
	private val resourceId: Int,
	private val times: List<ReminderTime>) : ArrayAdapter<ReminderTime>(context, resourceId, times) {

	override fun getCount() = times.size

	override fun getItem(position: Int) = times[position]

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val inflater = LayoutInflater.from(context)
		return convertView ?: inflater.inflate(resourceId, parent, false).apply {
			findViewById<TextView>(R.id.time_value).text = times[position].name
		}
	}

	override fun getFilter(): Filter = NonFilter()

	private class NonFilter : Filter() {
		override fun performFiltering(constraint: CharSequence?) = FilterResults()

		override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
	}
}