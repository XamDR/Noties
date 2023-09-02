package io.github.xamdr.noties.ui.settings

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.preference.PreferenceManager

@Composable
fun DefaultPreference(
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
	summary: Int,
	icon: ImageVector,
	key: String
) {
	val context = LocalContext.current
	val preferences = remember { PreferenceManager.getDefaultSharedPreferences(context) }
	var checked by remember { mutableStateOf(preferences.getBoolean(key, false)) }

	ListItem(
		headlineContent = {
			Text(text = stringResource(id = title))
		},
		supportingContent = {
			Text(text = stringResource(id = summary))
		},
		leadingContent = {
			Icon(imageVector = icon, contentDescription = null)
		},
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
fun ListPreference(
	title: Int,
	summary: Int,
	icon: ImageVector,
	entries: List<String>
) {
	ListItem(
		headlineContent = { Text(text = stringResource(id = title)) },
		supportingContent = { Text(text = stringResource(id = summary)) },
		leadingContent = { Icon(imageVector = icon, contentDescription = null) }
	)
}