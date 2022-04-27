package net.azurewebsites.eznotes.ui.settings

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.util.LayoutType
import net.azurewebsites.eznotes.util.SortMode

class UserPreferences(context: Context) {

	private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

	var isFirstRun = preferences.getBoolean(FIRST_TIME_PREFERENCE, true)
		set(value) = preferences.edit { putBoolean(FIRST_TIME_PREFERENCE, value) }

	var layoutType = LayoutType.from(preferences.getInt(LAYOUT_PREFERENCE, LayoutType.Linear.spanCount))
		set(value) = preferences.edit { value?.spanCount?.let { putInt(LAYOUT_PREFERENCE, it) } }

	var sortMode = SortMode.from(preferences.getInt(SORT_PREFERENCE, SortMode.LastEdit.value))
		set(mode) = preferences.edit { mode?.value?.let { putInt(SORT_PREFERENCE, it) } }

	var defaultDirectoryName = preferences.getString(DEFAULT_DIRECTORY_NAME, context.getString(R.string.general_folder))
		set(value) = preferences.edit { putString(DEFAULT_DIRECTORY_NAME, value) }

	companion object {
		private const val FIRST_TIME_PREFERENCE = "isFirstRun"
		private const val LAYOUT_PREFERENCE = "layoutPreference"
		private const val SORT_PREFERENCE = "sortPreference"
		private const val DEFAULT_DIRECTORY_NAME = "defaultDirectoryName"
	}
}
