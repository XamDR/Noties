package io.github.xamdr.noties.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.notes.LayoutType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceStorage @Inject constructor(@ApplicationContext context: Context) {

	private val preferences = lazy { PreferenceManager.getDefaultSharedPreferences(context) }

	var isOnboardingCompleted by BooleanPreference(preferences, PREF_ONBOARDING, false)

	var wallpaper by StringPreference(preferences, PREF_WALLPAPER, String.Empty)

	var layoutType by StringPreference(preferences, PREF_LAYOUT_NOTES, LayoutType.Linear.name)

	val appTheme by IntegerPreference(preferences, PREF_APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

	val appColor by IntegerPreference(preferences, PREF_APP_COLOR, R.color.blue_600)

	val urlsEnabled by BooleanPreference(preferences, PREF_URLS_ENABLED, true)

	companion object {
		const val PREF_APP_THEME = "pref_app_theme"
		const val PREF_APP_COLOR = "pref_app_color"
		const val PREF_URLS_ENABLED = "pref_urls_enabled"
		private const val PREF_ONBOARDING = "pref_onboarding"
		private const val PREF_LAYOUT_NOTES = "pref_layout_notes"
		private const val PREF_WALLPAPER = "pref_wallpaper"
	}
}
