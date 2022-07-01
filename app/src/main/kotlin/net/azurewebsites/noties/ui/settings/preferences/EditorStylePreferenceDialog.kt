package net.azurewebsites.noties.ui.settings.preferences

import android.content.DialogInterface
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.preference.PreferenceDialogFragmentCompat
import com.google.android.material.radiobutton.MaterialRadioButton
import net.azurewebsites.noties.R

class EditorStylePreferenceDialog : PreferenceDialogFragmentCompat() {

	private lateinit var editorStyles: RadioGroup
	private lateinit var blankStyle: MaterialRadioButton
	private lateinit var stripedStyle: MaterialRadioButton

	override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
		super.onPrepareDialogBuilder(builder)
		builder.setPositiveButton(null, null)
	}

	override fun onBindDialogView(view: View) {
		super.onBindDialogView(view)
		editorStyles = view.findViewById(R.id.editor_styles)
		blankStyle = view.findViewById(R.id.blank_style)
		stripedStyle = view.findViewById(R.id.striped_style)

		blankStyle.setOnClickListener { closeDialog() }
		stripedStyle.setOnClickListener { closeDialog() }

		if (preference is EditorStylePreference) {
			val value = (preference as EditorStylePreference).getPersistedString()
			when (value) {
				EditorStylePreference.BLANK -> blankStyle.isChecked = true
				EditorStylePreference.STRIPED -> stripedStyle.isChecked = true
			}
		}
	}

	override fun onDialogClosed(positiveResult: Boolean) {
		if (positiveResult) {
			val id = editorStyles.checkedRadioButtonId
			val checkedRadioButton = editorStyles.findViewById<MaterialRadioButton>(id)
			val newValue = when (checkedRadioButton) {
				blankStyle -> EditorStylePreference.BLANK
				stripedStyle -> EditorStylePreference.STRIPED
				else -> throw Exception("Unknown style.")
			}
			if (preference is EditorStylePreference) {
				if (preference.callChangeListener(newValue)) {
					(preference as EditorStylePreference).setPersistedString(newValue)
				}
			}
		}
	}

	private fun closeDialog() {
		onClick(dialog as DialogInterface, DialogInterface.BUTTON_POSITIVE)
		dismiss()
	}

	companion object {
		fun newInstance(key: String) = EditorStylePreferenceDialog().apply {
			arguments = bundleOf(ARG_KEY to key)
		}
	}
}