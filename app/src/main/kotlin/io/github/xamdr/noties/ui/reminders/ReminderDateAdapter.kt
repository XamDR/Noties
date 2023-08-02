package io.github.xamdr.noties.ui.reminders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import io.github.xamdr.noties.R

class ReminderDateAdapter(
	context: Context,
	private val resourceId: Int,
	private val dates: List<ReminderDate>) : ArrayAdapter<ReminderDate>(context, resourceId, dates) {

	override fun getCount() = dates.size

	override fun getItem(position: Int) = dates[position]

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val inflater = LayoutInflater.from(context)
		return convertView ?: inflater.inflate(resourceId, parent, false).apply {
			findViewById<TextView>(R.id.date_value).text = dates[position].name
		}
	}

	override fun getFilter(): Filter = NonFilter()

	private class NonFilter : Filter() {
		override fun performFiltering(constraint: CharSequence?) = FilterResults()

		override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
	}
}