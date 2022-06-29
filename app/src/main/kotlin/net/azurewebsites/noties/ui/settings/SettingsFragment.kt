package net.azurewebsites.noties.ui.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.helpers.setNightMode
import net.azurewebsites.noties.ui.settings.preferences.EditorStylePreference
import net.azurewebsites.noties.ui.settings.preferences.EditorStylePreferenceDialog
import net.azurewebsites.noties.ui.settings.preferences.FontSizePreference
import net.azurewebsites.noties.ui.settings.preferences.FontSizePreferenceDialog

class SettingsFragment : PreferenceFragmentCompat() {

	private val themeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
		when (key) {
			"app_theme" -> context?.setNightMode()
		}
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)
	}

	override fun onDisplayPreferenceDialog(preference: Preference) {
		if (parentFragmentManager.findFragmentByTag(FONT_SIZE_TAG) != null ||
			parentFragmentManager.findFragmentByTag(EDITOR_STYLE_TAG) != null) return

		when (preference) {
			is FontSizePreference -> {
				val dialog = FontSizePreferenceDialog.newInstance(preference.key)
				showDialog(dialog, FONT_SIZE_TAG)
			}
			is EditorStylePreference -> {
				val dialog = EditorStylePreferenceDialog.newInstance(preference.key)
				showDialog(dialog, EDITOR_STYLE_TAG)
			}
			else -> super.onDisplayPreferenceDialog(preference)
		}
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

	// We need to suppress the deprecation of the method setTargetFragment
	// because PreferenceFragmentCompat doesn't use yet the new Fragment
	// Manager APIs (setFragmentResultListener, etc.)
	@Suppress("DEPRECATION")
	private fun showDialog(dialog: PreferenceDialogFragmentCompat, tag: String) {
		dialog.setTargetFragment(this, 0)
		dialog.show(parentFragmentManager, tag)
	}

	companion object {
		private const val EDITOR_STYLE_TAG = "EditorStyleDialog"
		private const val FONT_SIZE_TAG = "FontSizeDialog"
	}
}