package io.github.xamdr.noties.ui.tags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.components.EmptyView
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
	tagsFromNote: List<String>,
	onNavigationIconClick: (List<String>) -> Unit,
	viewModel: TagsViewModel = hiltViewModel()
) {
	val tags by viewModel.getTags().collectAsState(initial = emptyList())
	var selectedTags by remember { mutableStateOf(value = tagsFromNote) }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = stringResource(id = R.string.tags_fragment_label)) },
				navigationIcon = {
					IconButton(onClick = { onNavigationIconClick(selectedTags) }) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.back_button_description)
						)
					}
				}
			)
		},
		content = { innerPadding ->
			if (tags.isEmpty()) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					EmptyView(
						icon = Icons.Outlined.Label,
						message = R.string.empty_tags_message
					)
				}
			}
			else {
				TagList(
					tags = tags,
					selectedTags = selectedTags,
					onTagSelected = { checked, tag ->
						if (checked) selectedTags += tag else selectedTags -= tag
					},
					modifier = Modifier.padding(innerPadding)
				)
			}
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsScreen() {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = stringResource(id = R.string.tags_fragment_label)) },
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
			TagList(modifier = Modifier.padding(innerPadding))
		}
	)
}

@DevicePreviews
@Composable
private fun TagsScreenPreview() = NotiesTheme { TagsScreen() }