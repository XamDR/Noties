package io.github.xamdr.noties.ui.settings.preferences

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.ColorAdapter
import io.github.xamdr.noties.ui.helpers.getIntArray

class ColorPreferenceDialog : PreferenceDialogFragmentCompat() {

	private lateinit var colors: List<Int>
	private lateinit var recyclerView: RecyclerView
	private lateinit var colorAdapter: ColorAdapter
	private var color = 0

	override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
		super.onPrepareDialogBuilder(builder)
		builder.setPositiveButton(null, null)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		colors = requireContext().getIntArray(R.array.app_colors).toList()
		colorAdapter = ColorAdapter(colors).apply {
			setOnColorSelectedListener { position -> closeDialog(position) }
		}
	}

	override fun onBindDialogView(view: View) {
		super.onBindDialogView(view)
		recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
			adapter = colorAdapter
		}
		if (preference is ColorPreference) {
			val value = (preference as ColorPreference).getPersistedInt()
			val position = colors.indexOf(value)
			colorAdapter.selectedPosition = position
		}
	}

	private fun closeDialog(position: Int) {
		color = colors[position]
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
		fun newInstance(key: String) = ColorPreferenceDialog().apply {
			arguments = bundleOf(ARG_KEY to key)
		}
	}
}