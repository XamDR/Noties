package net.azurewebsites.noties.ui.settings.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import net.azurewebsites.noties.R

class EditorStylePreference(context: Context, attrs: AttributeSet?) : DialogPreference(context, attrs) {

	override fun getSummary(): CharSequence {
		val value = getPersistedString(BLANK)
		return when (value) {
			BLANK -> context.getString(R.string.blank_style)
			STRIPED -> context.getString(R.string.striped_style)
			else -> throw Exception("Unknown style.")
		}
	}

	fun getPersistedString(): String = super.getPersistedString(BLANK)

	fun setPersistedString(value: String) {
		super.persistString(value)
		notifyChanged()
	}

	override fun getDialogLayoutResource() = R.layout.preference_dialog_editor_style

	companion object {
		const val BLANK = "Blank"
		const val STRIPED = "Striped"
	}
}