package net.azurewebsites.noties.ui.settings

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.azurewebsites.noties.ui.notes.SortMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceStorage @Inject constructor(@ApplicationContext context: Context) {

	private val preferences = lazy { PreferenceManager.getDefaultSharedPreferences(context) }

	var isOnboardingCompleted by BooleanPreference(preferences, PREF_ONBOARDING, false)

	var sortMode by StringPreference(preferences, PREF_SORTING_NOTES, SortMode.LastEdit.name)

	companion object {
		private const val PREF_ONBOARDING = "pref_onboarding"
		private const val PREF_SORTING_NOTES = "pref_sorting_notes"
	}
}
