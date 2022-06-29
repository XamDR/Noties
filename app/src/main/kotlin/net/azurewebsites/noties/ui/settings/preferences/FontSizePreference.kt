package net.azurewebsites.noties.ui.settings.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference

// Based on this StackOverflow answer: https://stackoverflow.com/a/61340341/8781554
class FontSizePreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs) {

	private val defaultValue = 16

	override fun getSummary() = getPersistedInt(defaultValue).toString()

	fun getPersistedInt() = super.getPersistedInt(defaultValue)

	fun setPersistInt(value: Int) {
		super.persistInt(value)
		notifyChanged()
	}
}