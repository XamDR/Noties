package io.github.xamdr.noties.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.components.CircularShapedBox
import io.github.xamdr.noties.ui.components.RadioButtonGroup
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun ListPreference(
	title: Int,
	dialogTitle: Int,
	icon: Icon,
	key: String,
	entries: Map<Int, Int>,
	onItemSelected: (Int) -> Unit
) {
	val context = LocalContext.current
	val preferences = remember { PreferenceManager.getDefaultSharedPreferences(context) }
	val items = entries.values.map { stringResource(id = it) }
	var selectedItem by remember {
		mutableStateOf(items[preferences.getInt(key, 0)])
	}
	var openDialog by rememberSaveable { mutableStateOf(false) }

	ListItem(
		headlineContent = { Text(text = stringResource(id = title)) },
		supportingContent = { Text(text = selectedItem) },
		leadingContent = {
			when (icon) {
				is Icon.Drawable -> Icon(painter = painterResource(id = icon.resource), contentDescription = null)
				is Icon.Vector -> Icon(imageVector = icon.imageVector, contentDescription = null)
			}
		},
		modifier = Modifier.clickable { openDialog = true }
	)
	if (openDialog) {
		AlertDialog(
			onDismissRequest = { openDialog = false },
			confirmButton = {},
			dismissButton = {
				TextButton(onClick = { openDialog = false }) {
					Text(text = stringResource(id = R.string.cancel_button))
				}
			},
			title = { Text(text = stringResource(id = dialogTitle)) },
			text = {
				RadioButtonGroup(
					items = items,
					selectedItem = selectedItem,
					onClick = { item ->
						selectedItem = item
						openDialog = false
						preferences.edit { putInt(key, items.indexOf(item)) }
						onItemSelected(items.indexOf(item))
					}
				)
			}
		)
	}
}

@Composable
private fun ListPreferenceDialog() {
	val items = listOf("Option 1", "Option 2", "Option 3")
	AlertDialog(
		onDismissRequest = {},
		confirmButton = {},
		dismissButton = {
			TextButton(onClick = {}) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = { Text(text = stringResource(id = R.string.app_theme_dialog_title)) },
		text = {
			RadioButtonGroup(
				items = items,
				selectedItem = items[0],
				onClick = {}
			)
		}
	)
}

@DevicePreviews
@Composable
private fun ListPreferenceDialogPreview() = NotiesTheme { ListPreferenceDialog() }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPreference(
	title: Int,
	dialogTitle: Int,
	icon: Icon,
	key: String,
	entries: List<Int>
) {
	val context = LocalContext.current
	val preferences = remember { PreferenceManager.getDefaultSharedPreferences(context) }
	var selectedEntry by remember { mutableIntStateOf(preferences.getInt(key, entries[0])) }
	var openDialog by rememberSaveable { mutableStateOf(false) }

	ListItem(
		headlineContent = { Text(text = stringResource(id = title)) },
		leadingContent = {
			when (icon) {
				is Icon.Drawable -> Icon(painter = painterResource(id = icon.resource), contentDescription = null)
				is Icon.Vector -> Icon(imageVector = icon.imageVector, contentDescription = null)
			}
		},
		trailingContent = { CircularShapedBox(color = colorResource(id = selectedEntry), size = 32.dp) },
		modifier = Modifier.clickable { openDialog = true }
	)
	if (openDialog) {
		AlertDialog(
			onDismissRequest = { openDialog = false },
			confirmButton = {},
			dismissButton = {
				TextButton(onClick = { openDialog = false }) {
					Text(text = stringResource(id = R.string.cancel_button))
				}
			},
			title = { Text(text = stringResource(id = dialogTitle)) },
			text = {
				FlowRow(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceEvenly,
					verticalArrangement = Arrangement.spacedBy(16.dp),
					maxItemsInEachRow = entries.size / 2
				) {
					entries.forEach { entry ->
						CircularShapedBox(
							color = colorResource(id = entry),
							size = 48.dp,
							modifier = Modifier.padding(4.dp),
							onClick = {
								selectedEntry = entry
								openDialog = false
								preferences.edit { putInt(key, entry) }
							}
						) {
							if (selectedEntry == entry) {
								Icon(
									imageVector = Icons.Outlined.Check,
									contentDescription = stringResource(id = R.string.app_color_chosen),
									modifier = Modifier.size(32.dp)
								)
							}
						}
					}
				}
			}
		)
	}
}

@Composable
private fun ColorPreference() {
	ListItem(
		headlineContent = { Text(text = "Color Preference") },
		leadingContent = { Icon(imageVector = Icons.Outlined.BrightnessMedium, contentDescription = null) },
		trailingContent = { CircularShapedBox(color = Color.Blue, size = 32.dp) },
		modifier = Modifier.clickable {}
	)
}

@DevicePreviews
@Composable
private fun ColorPreferencePreview() = NotiesTheme { ColorPreference() }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPreferenceDialog() {
	val entries = listOf(
		R.color.blue_600,
		R.color.pink_600,
		R.color.purple_600,
		R.color.teal_600,
		R.color.orange_600,
		R.color.black
	)
	AlertDialog(
		onDismissRequest = {},
		confirmButton = {},
		dismissButton = {
			TextButton(onClick = {}) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = { Text(text = stringResource(id = R.string.app_color_dialog_title)) },
		text = {
			FlowRow(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly,
				verticalArrangement = Arrangement.spacedBy(16.dp),
				maxItemsInEachRow = 3
			) {
				entries.forEach { entry ->
					CircularShapedBox(
						color = colorResource(id = entry),
						size = 48.dp,
						modifier = Modifier.padding(4.dp),
						onClick = {}
					) {
						if (entry == entries[0]) {
							Icon(
								imageVector = Icons.Outlined.Check,
								contentDescription = stringResource(id = R.string.app_color_chosen),
								modifier = Modifier.size(32.dp)
							)
						}
					}
				}
			}
		}
	)
}

@DevicePreviews
@Composable
private fun ColorPreferenceDialogPreview() = NotiesTheme { ColorPreferenceDialog() }