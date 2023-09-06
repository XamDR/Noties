package io.github.xamdr.noties.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun PreferenceHeader(title: Int) {
	Text(
		text = stringResource(id = title),
		modifier = Modifier.padding(horizontal = 16.dp),
		color = MaterialTheme.colorScheme.primary,
		style = MaterialTheme.typography.titleMedium
	)
}

@Composable
fun SimplePreference(
	title: Int,
	summary: Int,
	icon: ImageVector
) {
	ListItem(
		headlineContent = { Text(text = stringResource(id = title)) },
		supportingContent = { Text(text = stringResource(id = summary)) },
		leadingContent = { Icon(imageVector = icon, contentDescription = null) }
	)
}

@Composable
fun SwitchPreference(
	title: Int,
	summaryOn: Int,
	summaryOff: Int,
	icon: ImageVector,
	key: String
) {
	val context = LocalContext.current
	val preferences = remember { PreferenceManager.getDefaultSharedPreferences(context) }
	var checked by remember { mutableStateOf(preferences.getBoolean(key, false)) }
	val summary = if (checked) stringResource(id = summaryOn) else stringResource(id = summaryOff)

	ListItem(
		headlineContent = { Text(text = stringResource(id = title)) },
		supportingContent = { Text(text = summary) },
		leadingContent = { Icon(imageVector = icon, contentDescription = null) },
		trailingContent = {
			Switch(
				checked = checked,
				onCheckedChange = { value ->
					preferences.edit { putBoolean(key, value) }
					checked = value
				}
			)
		}
	)
}

@Composable
private fun SwitchPreference() {
	ListItem(
		headlineContent = { Text(text = "Switch preference") },
		supportingContent = { Text(text = "Enabled") },
		leadingContent = { Icon(imageVector = Icons.Outlined.Link, contentDescription = null) },
		trailingContent = {
			Switch(
				checked = true,
				onCheckedChange = {}
			)
		}
	)
}

@DevicePreviews
@Composable
private fun SwitchPreferencePreview() = NotiesTheme { SwitchPreference() }