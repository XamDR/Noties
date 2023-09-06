package io.github.xamdr.noties.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigationIconClick: () -> Unit) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = stringResource(id = R.string.settings)) },
				navigationIcon = {
					IconButton(onClick = onNavigationIconClick) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.back_button_description)
						)
					}
				}
			)
		},
		content = { innerPadding ->
			PreferenceList(
				modifier = Modifier.padding(innerPadding),
				items = DEFAULT_SETTINGS
			)
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen() {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = stringResource(id = R.string.settings)) },
				navigationIcon = {
					IconButton(onClick = {}) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.back_button_description)
						)
					}
				}
			)
		},
		content = { innerPadding ->
			PreferenceList(modifier = Modifier.padding(innerPadding))
		}
	)
}

@DevicePreviews
@Composable
private fun SettingsScreenPreview() = NotiesTheme { SettingsScreen() }