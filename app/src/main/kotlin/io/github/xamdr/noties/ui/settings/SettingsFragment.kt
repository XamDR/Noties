package io.github.xamdr.noties.ui.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.setNightMode
import io.github.xamdr.noties.ui.settings.preferences.*

class SettingsFragment : PreferenceFragmentCompat() {

	private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
		when (key) {
			"app_theme" -> requireContext().setNightMode()
			"app_color" -> requireActivity().recreate()
		}
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)
	}

	override fun onDisplayPreferenceDialog(preference: Preference) {
		if (parentFragmentManager.findFragmentByTag(COLOR_TAG) != null ||
			parentFragmentManager.findFragmentByTag(EDITOR_STYLE_TAG) != null) return

		when (preference) {
			is ColorPreference -> {
				val dialog = ColorPreferenceDialog.newInstance(preference.key)
				showDialog(dialog, COLOR_TAG)
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
		preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
	}

	override fun onResume() {
		super.onResume()
		preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
	}

	// We need to suppress the deprecation of the method setTargetFragment
	// because PreferenceFragmentCompat doesn't use yet the new Fragment
	// Manager APIs (setFragmentResultListener, etc.)
	@Suppress("DEPRECATION")
	private fun showDialog(dialog: PreferenceDialogFragmentCompat, tag: String) {
		dialog.setTargetFragment(this, 0)
		dialog.show(parentFragmentManager, tag)
	}

	private companion object {
		private const val COLOR_TAG = "COLOR_TAG"
		private const val EDITOR_STYLE_TAG = "EDITOR_STYLE_TAG"
	}
}