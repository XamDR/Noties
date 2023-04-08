package io.github.xamdr.noties.ui.settings.preferences

import android.content.Context
import android.view.View
import android.widget.NumberPicker
import androidx.core.os.bundleOf
import androidx.preference.PreferenceDialogFragmentCompat

// Based on this StackOverflow answer: https://stackoverflow.com/a/61340341/8781554
class FontSizePreferenceDialog : PreferenceDialogFragmentCompat() {
	private lateinit var numberPicker: NumberPicker

	override fun onCreateDialogView(context: Context): View {
		numberPicker = NumberPicker(context).apply {
			displayedValues = FONT_SIZES
			minValue = 0
			maxValue = FONT_SIZES.size - 1
			// make the number picker non editable
			descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
		}
		return numberPicker
	}

	override fun onBindDialogView(view: View) {
		super.onBindDialogView(view)
		numberPicker.value =
			numberPicker.displayedValues.indexOf(
				(preference as FontSizePreference).getPersistedInt().toString()
			)
	}

	override fun onDialogClosed(positiveResult: Boolean) {
		if (positiveResult) {
			numberPicker.clearFocus()
			val newValue = numberPicker.displayedValues[numberPicker.value]

			if (preference.callChangeListener(newValue)) {
				(preference as FontSizePreference).setPersistInt(newValue.toInt())
			}
		}
	}

	companion object {
		private val FONT_SIZES = arrayOf(
			"10", "11", "12", "14", "16", "18", "20", "22", "24", "28",
			"32", "36", "40", "44", "48", "56", "64", "72", "80"
		)

		fun newInstance(key: String) = FontSizePreferenceDialog().apply {
			arguments = bundleOf(ARG_KEY to key)
		}
	}
}