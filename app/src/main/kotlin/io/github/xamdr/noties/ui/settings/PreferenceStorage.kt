package io.github.xamdr.noties.ui.settings

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.xamdr.noties.ui.notes.LayoutType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceStorage @Inject constructor(@ApplicationContext context: Context) {

	private val preferences = lazy { PreferenceManager.getDefaultSharedPreferences(context) }

	var isOnboardingCompleted by BooleanPreference(preferences, PREF_ONBOARDING, false)

	var layoutType by StringPreference(preferences, PREF_LAYOUT_NOTES, LayoutType.Linear.name)

	var isExactAlarmEnabled by BooleanPreference(preferences, PREF_EXACT_ALARM, false)

	var wallpaper by StringPreference(preferences, PREF_WALLPAPER, String.Empty)

	val hiperlinksEnabled by BooleanPreference(preferences, PREF_HIPERLINKS_ENABLED, false)

	companion object {
		const val PREF_APP_THEME = "pref_app_theme"
		const val PREF_APP_COLOR = "pref_app_color"
		const val PREF_HIPERLINKS_ENABLED = "pref_hiperlinks_enabled"
		private const val PREF_ONBOARDING = "pref_onboarding"
		private const val PREF_LAYOUT_NOTES = "pref_layout_notes"
		private const val PREF_EXACT_ALARM = "pref_exact_alarm"
		private const val PREF_WALLPAPER = "pref_wallpaper"
	}
}
