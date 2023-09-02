package io.github.xamdr.noties.ui.settings

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.xamdr.noties.R

sealed class PreferenceItem(@StringRes open val title: Int) {
	data class Category(@StringRes override val title: Int) : PreferenceItem(title)
	data class Setting(
		@StringRes override val title: Int,
		val icon: ImageVector,
		@StringRes val summary: Int,
		val type: PreferenceType,
		val key: String) : PreferenceItem(title)
}

sealed interface PreferenceType {
	data object Default : PreferenceType
	data object Switch : PreferenceType
	data object List : PreferenceType
	data object Color : PreferenceType
}

val DEFAULT_SETTINGS = listOf(
	PreferenceItem.Category(title = R.string.personalization_header),
	PreferenceItem.Setting(
		title = R.string.app_theme_title,
		icon = Icons.Outlined.BrightnessMedium,
		summary = R.string.mode_night_follow_system,
		type = PreferenceType.List,
		key = PreferenceStorage.PREF_APP_THEME
	),
	PreferenceItem.Setting(
		title = R.string.app_color,
		icon = Icons.Outlined.Palette,
		summary = 0,
		type = PreferenceType.Color,
		key = PreferenceStorage.PREF_APP_COLOR
	),
	PreferenceItem.Category(title = R.string.editor_header),
	PreferenceItem.Setting(
		title = R.string.enable_links,
		icon = Icons.Outlined.Link,
		summary = R.string.links_enabled,
		type = PreferenceType.Switch,
		key = PreferenceStorage.PREF_HIPERLINKS_ENABLED
	),
	PreferenceItem.Category(title = R.string.about_header),
	PreferenceItem.Setting(
		title = R.string.developer,
		icon = Icons.Outlined.Person,
		summary = R.string.developer_name,
		type = PreferenceType.Default,
		key = String.Empty
	),
	PreferenceItem.Setting(
		title = R.string.version,
		icon = Icons.Outlined.Info,
		summary = R.string.app_version,
		type = PreferenceType.Default,
		key = String.Empty
	)
)