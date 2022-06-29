package net.azurewebsites.noties.ui.settings.preferences

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.helpers.ColorAdapter

class ColorPreferenceDialog : PreferenceDialogFragmentCompat() {

	private lateinit var colors: List<Int>
	private lateinit var recyclerView: RecyclerView
	private var color = 0

	override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
		super.onPrepareDialogBuilder(builder)
		builder.setPositiveButton(null, null)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		context?.resources?.getIntArray(R.array.colors)?.toList()?.let { colors = it }
	}

	override fun onBindDialogView(view: View) {
		super.onBindDialogView(view)
		recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
			adapter = ColorAdapter(colors)
		}
		if (preference is ColorPreference) {
			val value = (preference as ColorPreference).getPersistedInt()
			val position = colors.indexOf(value)
		}
	}

	private fun closeDialog() {
		onClick(dialog as DialogInterface, DialogInterface.BUTTON_POSITIVE)
		dismiss()
	}

	override fun onDialogClosed(positiveResult: Boolean) {
		if (positiveResult) {
			if (preference is ColorPreference) {
				if (preference.callChangeListener(color)) {
					(preference as ColorPreference).setPersistedInt(color)
				}
			}
		}
	}

	companion object {
		fun newInstance(key: String): ColorPreferenceDialog {
			val fragment = ColorPreferenceDialog()
			val bundle = Bundle().apply {
				putString(ARG_KEY, key)
			}
			fragment.arguments = bundle
			return fragment
		}
	}
}