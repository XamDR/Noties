package net.azurewebsites.noties.ui.settings.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.helpers.getIntArray

class ColorPreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs) {

	init {
		widgetLayoutResource = R.layout.preference_color_layout
	}

	private val defaultValue = context.getIntArray(R.array.app_colors)[0]

	fun getPersistedInt(): Int = super.getPersistedInt(defaultValue)

	fun setPersistedInt(value: Int) {
		super.persistInt(value)
		notifyChanged()
	}

	override fun onBindViewHolder(holder: PreferenceViewHolder) {
		super.onBindViewHolder(holder)
		val imageView = holder.findViewById(R.id.color_view) as ImageView
		imageView.setBackgroundColor(getPersistedInt())
	}

	override fun getDialogLayoutResource() = R.layout.preference_dialog_color
}