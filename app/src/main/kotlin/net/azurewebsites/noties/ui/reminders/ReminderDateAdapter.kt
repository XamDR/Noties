package net.azurewebsites.noties.ui.reminders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.ViewCompat
import net.azurewebsites.noties.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ReminderDateAdapter(
	context: Context,
	private val resourceId: Int,
	private val dates: List<ReminderDate>) : ArrayAdapter<ReminderDate>(context, resourceId, dates) {

	override fun getCount() = dates.size

	override fun getItem(position: Int) = dates[position]

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val inflater = LayoutInflater.from(context)
		return convertView ?: inflater.inflate(resourceId, parent, false).apply {
			dates[position].value?.let {
				findViewById<TextView>(R.id.date_value).text =
					it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
			}
		}
	}

	override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
		val inflater = LayoutInflater.from(context)
		val view = convertView ?: inflater.inflate(R.layout.spinner_date_dropwdown_item, parent, false)
		bindName(view, position, parent)
		return view
	}

	fun setOnItemClickListener(listener: AdapterView.OnItemClickListener) {
		this.listener = listener
	}

	private var listener: AdapterView.OnItemClickListener? = null

	private fun bindName(view: View, position: Int, parent: ViewGroup) {
		view.findViewById<TextView>(R.id.date_name).apply {
			text = dates[position].name
			setOnClickListener {
				ViewCompat.postOnAnimationDelayed(it, {
					listener?.onItemClick(parent as AdapterView<*>, it, position, getItemId(position))
				}, 100)
			}
		}
	}
}