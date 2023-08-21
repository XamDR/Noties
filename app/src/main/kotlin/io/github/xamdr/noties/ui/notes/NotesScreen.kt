package io.github.xamdr.noties.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
	query: String,
	onQueryChange: (String) -> Unit,
	onSearch: (String) -> Unit,
	active: Boolean,
	onActiveChange: (Boolean) -> Unit,
	onLeadingIconClick: () -> Unit,
	onTrailingIconClick: () -> Unit,
	onFabClick: () -> Unit,
	viewModel: NotesViewModel,
	searchContent: @Composable (ColumnScope.() -> Unit)
) {
	val notes by viewModel.getAllNotes().collectAsStateWithLifecycle(initialValue = null)

	Scaffold(
		topBar = {
			SearchBar(
				query = query,
				onQueryChange = onQueryChange,
				onSearch = onSearch,
				active = active,
				onActiveChange = onActiveChange,
				placeholder = { Text(text = stringResource(id = R.string.search_notes)) },
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp),
				leadingIcon = {
					IconButton(onClick = onLeadingIconClick) {
						Icon(
							imageVector = Icons.Filled.Menu,
							contentDescription = "Open drawer"
						)
					}
				},
				trailingIcon = {
					IconButton(onClick = onTrailingIconClick) {
						Icon(
							imageVector = Icons.Outlined.GridView,
							contentDescription = stringResource(id = R.string.grid_layout_view)
						)
					}
				},
				content = searchContent
			)
		},
		floatingActionButtonPosition = FabPosition.End,
		floatingActionButton = {
			FloatingActionButton(onClick = onFabClick) {
				Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(id = R.string.add_note))
			}
		},
		content = { innerPadding ->
			if (notes.isNullOrEmpty()) {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					if (notes == null) {
						CircularProgressIndicator()
					}
					else {
						Text(text = stringResource(id = R.string.empty_notes_message))
					}
				}
			}
			else {
				notes?.let {
					NoteList(modifier = Modifier.padding(innerPadding), notes = it)
				}
			}
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesScreen() {
	Scaffold(
		topBar = {
			SearchBar(
				query = "",
				onQueryChange = {},
				onSearch = {},
				active = false,
				onActiveChange = {},
				placeholder = { Text(text = stringResource(id = R.string.search_notes)) },
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp),
				leadingIcon = {
					IconButton(onClick = {}) {
						Icon(
							imageVector = Icons.Filled.Menu,
							contentDescription = "Open drawer"
						)
					}
				},
				trailingIcon = {
					IconButton(onClick = {}) {
						Icon(
							imageVector = Icons.Outlined.GridView,
							contentDescription = stringResource(id = R.string.grid_layout_view)
						)
					}
				},
				content = {}
			)
		},
		floatingActionButtonPosition = FabPosition.End,
		floatingActionButton = {
			FloatingActionButton(onClick = {}) {
				Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(id = R.string.add_note))
			}
		},
		content = { innerPadding ->
			NoteList(
				modifier = Modifier.padding(innerPadding),
				notes = emptyList()
			)
		}
	)
}

@DevicePreviews
@Composable
private fun NotesScreenPreview() = NotiesTheme { NotesScreen() }