package net.azurewebsites.noties.ui.settings.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import net.azurewebsites.noties.R

class ColorPreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs) {

	init {
		widgetLayoutResource = R.layout.color_item
	}

	private val defaultValue = context.resources.getIntArray(R.array.colors)[0]

	fun getPersistedInt(): Int = super.getPersistedInt(defaultValue)

	fun setPersistedInt(value: Int) {
		super.persistInt(value)
		notifyChanged()
	}

	override fun onBindViewHolder(holder: PreferenceViewHolder) {
		super.onBindViewHolder(holder)
		val imageView = holder.findViewById(R.id.color) as ImageView
		imageView.setBackgroundColor(getPersistedInt())
	}

	override fun getDialogLayoutResource() = R.layout.preference_dialog_color
}