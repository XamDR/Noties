package io.github.xamdr.noties.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.openUrl
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun PreferenceList(
	modifier: Modifier,
	items: List<PreferenceItem>
) {
	val context = LocalContext.current
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			key = null,
			items = items
		) { item ->
			PreferenceItem(
				item = item,
				onClick = {
					when (item.preference?.tag) {
						Constants.LINKEDIN_PROFILE -> context.openUrl(Constants.LINKEDIN_PROFILE)
						Constants.GITHUB_REPOSITORY -> context.openUrl(Constants.GITHUB_REPOSITORY)
						else -> {}
					}
				}
			)
		}
	}
}

@Composable
fun PreferenceList(modifier: Modifier = Modifier) {
	val items = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
	Column(
		modifier = modifier.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items.forEach { item ->
			if (item % 3 == 0) {
				PreferenceHeader(title = R.string.about_app)
			}
			else {
				SimplePreference(
					title = R.string.developer_name,
					summary = R.string.developer_name,
					icon = Icon.Vector(imageVector = Icons.Outlined.Person)
				)
			}
		}
	}
}

@DevicePreviews
@Composable
private fun PreferenceListPreview() = NotiesTheme { PreferenceList() }

@Composable
private fun PreferenceItem(
	item: PreferenceItem,
	onClick: (() -> Unit)? = null
) {
	when (item) {
		is PreferenceItem.Category -> PreferenceHeader(title = item.category.title)
		is PreferenceItem.ColorPreference -> ColorPreference(
			title = item.preference.title,
			dialogTitle = item.dialogTitle,
			icon = item.preference.icon,
			key = item.preference.key,
			entries = item.entries
		)
		is PreferenceItem.ListPreference -> ListPreference(
			title = item.preference.title,
			dialogTitle = item.dialogTitle,
			icon = item.preference.icon,
			key = item.preference.key,
			entries = item.entries,
			onItemSelected = ::setNightMode
		)
		is PreferenceItem.SimplePreference -> SimplePreference(
			title = item.preference.title,
			summary = item.preference.summary,
			icon = item.preference.icon,
			onClick = if (item.preference.tag != null) onClick else null
		)
		is PreferenceItem.SwitchPreference -> SwitchPreference(
			title = item.preference.title,
			summaryOn = item.summaryOn,
			summaryOff = item.summaryOff,
			icon = item.preference.icon,
			key = item.preference.key
		)
	}
}