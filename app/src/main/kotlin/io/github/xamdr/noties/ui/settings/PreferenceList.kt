package io.github.xamdr.noties.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun PreferenceList(modifier: Modifier, items: List<PreferenceItem>) {
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			key = { index -> items[index].title },
			count = items.size
		) { index -> PreferenceItem(item = items[index]) }
	}
}

@Composable
private fun PreferenceList() {
	PreferenceList(modifier = Modifier, items = DEFAULT_SETTINGS)
}

@DevicePreviews
@Composable
private fun PreferenceListPreview() = NotiesTheme { PreferenceList() }

@Composable
private fun PreferenceItem(item: PreferenceItem) {
	when (item) {
		is PreferenceItem.Category -> {
			Text(
				text = stringResource(id = item.title),
				modifier = Modifier.padding(horizontal = 16.dp),
				style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
			)
		}
		is PreferenceItem.Setting -> {
			when (item.type) {
				PreferenceType.Color -> {}
				PreferenceType.Default -> {
					DefaultPreference(
						title = item.title,
						summary = item.summary,
						icon = item.icon
					)
				}
				PreferenceType.List -> {
					ListPreference(
						title = item.title,
						summary = item.summary,
						icon = item.icon,
						entries = emptyList()
					)
				}
				PreferenceType.Switch -> {
					SwitchPreference(
						title = item.title,
						summary = item.summary,
						icon = item.icon,
						key = item.key
					)
				}
			}
		}
	}
}