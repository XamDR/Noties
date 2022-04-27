package net.azurewebsites.eznotes.ui.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.ui.helpers.setNightMode

class SettingsFragment : PreferenceFragmentCompat() {

	private val themeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
		when (key) {
			"app_theme" -> context?.setNightMode()
		}
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setDivider(ColorDrawable(Color.TRANSPARENT))
		setDividerHeight(0)
		listView.isVerticalScrollBarEnabled = false
	}

	override fun onPause() {
		super.onPause()
		preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(themeListener)
	}

	override fun onResume() {
		super.onResume()
		preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(themeListener)
	}

	companion object {
		private const val EDITOR_STYLE_DIALOG_TAG = "PaperStyleDialog"
		private const val NUMBER_PICKER_DIALOG_TAG = "NumberPickerDialog"
	}
}