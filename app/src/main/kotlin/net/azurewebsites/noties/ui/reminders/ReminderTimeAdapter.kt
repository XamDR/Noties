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

class ReminderTimeAdapter(
	context: Context,
	private val resourceId: Int,
	private val times: List<ReminderTime>) : ArrayAdapter<ReminderTime>(context, resourceId, times) {

	override fun getCount() = times.size

	override fun getItem(position: Int) = times[position]

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val inflater = LayoutInflater.from(context)
		return convertView ?: inflater.inflate(resourceId, parent, false).apply {
			times[position].value?.let {
				findViewById<TextView>(R.id.time_value).text =
					it.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
			}
		}
	}

	override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
		val inflater = LayoutInflater.from(context)
		val view = convertView ?: inflater.inflate(R.layout.spinner_time_dropdown_item, parent, false)
		bindName(view, position, parent)
		return view
	}

	fun setOnItemClickListener(listener: AdapterView.OnItemClickListener) {
		this.listener = listener
	}

	private var listener: AdapterView.OnItemClickListener? = null

	private fun bindName(view: View, position: Int, parent: ViewGroup) {
		view.findViewById<TextView>(R.id.time_name).apply {
			text = times[position].name
			setOnClickListener {
				ViewCompat.postOnAnimationDelayed(it, {
					listener?.onItemClick(parent as AdapterView<*>, it, position, getItemId(position))
				}, 100)
			}
		}
	}
}