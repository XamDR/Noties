package io.github.xamdr.noties.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun PreferenceList(
	modifier: Modifier,
	items: List<PreferenceItem>
) {
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			key = null,
			count = items.size
		) { index -> PreferenceItem(item = items[index]) }
	}
}

@Composable
fun PreferenceList(modifier: Modifier = Modifier) {

}

@DevicePreviews
@Composable
private fun PreferenceListPreview() = NotiesTheme { PreferenceList() }

@Composable
private fun PreferenceItem(item: PreferenceItem) {
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
			entries = item.entries
		)
		is PreferenceItem.SimplePreference -> SimplePreference(
			title = item.preference.title,
			summary = item.preference.summary,
			icon = item.preference.icon
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