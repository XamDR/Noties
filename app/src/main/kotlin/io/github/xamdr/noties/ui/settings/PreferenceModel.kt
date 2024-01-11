package io.github.xamdr.noties.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.os.BuildCompat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.Constants

sealed interface Icon {
	data class Vector(val imageVector: ImageVector) : Icon
	@JvmInline value class Drawable(@DrawableRes val resource: Int) : Icon
}

data class Preference(
	val key: String,
	@StringRes val title: Int,
	@StringRes val summary: Int? = null,
	val icon: Icon,
	val tag: Any? = null
)

data class PreferenceCategory(@StringRes val title: Int)

sealed class PreferenceItem(open val preference: Preference?) {

	data class Category(val category: PreferenceCategory) : PreferenceItem(null)

	data class SimplePreference(override val preference: Preference) : PreferenceItem(preference)

	data class SwitchPreference(
		override val preference: Preference,
		val summaryOn: Int,
		val summaryOff: Int) : PreferenceItem(preference)

	data class ListPreference(
		override val preference: Preference,
		val dialogTitle: Int,
		val entries: Map<Int, Int>) : PreferenceItem(preference)

	data class ColorPreference(
		override val preference: Preference,
		val dialogTitle: Int,
		val entries: List<Int>) : PreferenceItem(preference)
}

@Suppress("DEPRECATION")
val DEFAULT_SETTINGS = listOf(
	PreferenceItem.Category(category = PreferenceCategory(title = R.string.personalization_header)),
	PreferenceItem.ListPreference(
		preference = Preference(
			key = PreferenceStorage.PREF_APP_THEME,
			title = R.string.app_theme_title,
			icon = Icon.Vector(imageVector = Icons.Outlined.BrightnessMedium)
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
			icon = Icon.Vector(imageVector = Icons.Outlined.Palette)
		),
		dialogTitle = R.string.app_color_dialog_title,
		entries = listOf(
			R.color.blue_600,
			R.color.pink_600,
			R.color.purple_600,
			R.color.teal_600,
			R.color.orange_600,
			R.color.black
		)
	),
	PreferenceItem.Category(category = PreferenceCategory(title = R.string.editor_header)),
	PreferenceItem.SwitchPreference(
		preference = Preference(
			key = PreferenceStorage.PREF_URLS_ENABLED,
			title = R.string.enable_links,
			icon = Icon.Vector(imageVector = Icons.Outlined.Link)
		),
		summaryOn = R.string.links_enabled,
		summaryOff = R.string.links_disabled
	),
	PreferenceItem.Category(category = PreferenceCategory(title = R.string.about_app)),
	PreferenceItem.SimplePreference(
		preference = Preference(
			key = String.Empty,
			title = R.string.version,
			summary = R.string.app_version,
			icon = Icon.Vector(imageVector = Icons.Outlined.Info)
		)
	),
	PreferenceItem.SimplePreference(
		preference = Preference(
			key = String.Empty,
			title = R.string.app_privacy_policy,
			icon = Icon.Vector(imageVector = Icons.Outlined.PrivacyTip)
		)
	),
	PreferenceItem.SimplePreference(
		preference = Preference(
			key = String.Empty,
			title = R.string.app_github,
			icon = Icon.Drawable(resource = R.drawable.ic_fab_github),
			tag = Constants.GITHUB_REPOSITORY
		)
	),
	PreferenceItem.Category(category = PreferenceCategory(title = R.string.about_developer)),
	PreferenceItem.SimplePreference(
		preference = Preference(
			key = String.Empty,
			title = R.string.developer_name,
			summary = R.string.developer_email,
			icon = Icon.Vector(imageVector = Icons.Outlined.Person)
		)
	),
	PreferenceItem.SimplePreference(
		preference = Preference(
			key = String.Empty,
			title = R.string.developer_linkedin,
			icon = Icon.Drawable(resource = R.drawable.ic_fab_linkedin),
			tag = Constants.LINKEDIN_PROFILE
		)
	),
)