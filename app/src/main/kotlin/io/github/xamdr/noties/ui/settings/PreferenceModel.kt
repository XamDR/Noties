package io.github.xamdr.noties.ui.settings

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.os.BuildCompat
import io.github.xamdr.noties.R

data class Preference(
	val key: String,
	@StringRes val title: Int,
	@StringRes val summary: Int = 0,
	val icon: ImageVector
)

data class PreferenceCategory(@StringRes val title: Int)

sealed interface PreferenceItem {
	data class Category(val category: PreferenceCategory) : PreferenceItem

	data class SimplePreference(val preference: Preference) : PreferenceItem

	data class SwitchPreference(
		val preference: Preference,
		val summaryOn: Int,
		val summaryOff: Int) : PreferenceItem

	data class ListPreference(
		val preference: Preference,
		val dialogTitle: Int,
		val entries: Map<Int, Int>) : PreferenceItem

	data class ColorPreference(
		val preference: Preference,
		val dialogTitle: Int,
		val entries: List<Int>) : PreferenceItem
}

@Suppress("DEPRECATION")
val DEFAULT_SETTINGS = listOf(
	PreferenceItem.Category(category = PreferenceCategory(title = R.string.personalization_header)),
	PreferenceItem.ListPreference(
		preference = Preference(
			key = PreferenceStorage.PREF_APP_THEME,
			title = R.string.app_theme_title,
			icon = Icons.Outlined.BrightnessMedium,
		),
		dialogTitle = R.string.app_theme_dialog_title,
		entries = mapOf(
			AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM + 1 to
					if (BuildCompat.isAtLeastQ()) R.string.mode_night_follow_system else R.string.mode_night_auto_battery,
			AppCompatDelegate.MODE_NIGHT_NO to R.string.mode_night_no,
			AppCompatDelegate.MODE_NIGHT_YES to R.string.mode_night_yes
		)
	),
	PreferenceItem.ColorPreference(
		preference = Preference(
			key = PreferenceStorage.PREF_APP_COLOR,
			title = R.string.app_color_title,
			icon = Icons.Outlined.Palette
		),
		dialogTitle = R.string.app_color_dialog_title,
		entries = listOf(
			R.color.blue_600,
			R.color.red_600,
			R.color.pink_600,
			R.color.purple_600,
			R.color.teal_600,
			R.color.green_600,
			R.color.yellow_200,
			R.color.brown_600,
			R.color.gray_700
		)
	),
	PreferenceItem.Category(category = PreferenceCategory(title = R.string.editor_header)),
	PreferenceItem.SwitchPreference(
		preference = Preference(
			key = PreferenceStorage.PREF_HIPERLINKS_ENABLED,
			title = R.string.enable_links,
			icon = Icons.Outlined.Link
		),
		summaryOn = R.string.links_enabled,
		summaryOff = R.string.links_disabled
	),
	PreferenceItem.Category(category = PreferenceCategory(title = R.string.about_header)),
	PreferenceItem.SimplePreference(
		preference = Preference(
			key = String.Empty,
			title = R.string.developer,
			summary = R.string.developer_name,
			icon = Icons.Outlined.Person
		)
	),
	PreferenceItem.SimplePreference(
		preference = Preference(
			key = String.Empty,
			title = R.string.version,
			summary = R.string.app_version,
			icon = Icons.Outlined.Info,
		)
	)
)