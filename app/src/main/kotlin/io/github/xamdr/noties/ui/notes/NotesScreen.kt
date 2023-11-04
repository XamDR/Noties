package io.github.xamdr.noties.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.components.EmptyView
import io.github.xamdr.noties.ui.components.OverflowMenu
import io.github.xamdr.noties.ui.editor.NoteAction
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import io.github.xamdr.noties.ui.helpers.rememberMutableStateList
import io.github.xamdr.noties.ui.media.ActionItem
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
	screen: Screen,
	query: String,
	onQueryChange: (String) -> Unit,
	onSearch: (String) -> Unit,
	active: Boolean,
	onActiveChange: (Boolean) -> Unit,
	onLeadingIconClick: () -> Unit,
	onTrailingIconClick: () -> Unit,
	onNavigationIconClick: () -> Unit,
	onFabClick: () -> Unit,
	onItemClick: (Note) -> Unit,
	noteAction: NoteAction,
	searchContent: @Composable (ColumnScope.() -> Unit),
	viewModel: NotesViewModel = hiltViewModel()
) {
	val notes by viewModel.getNotes(screen).collectAsStateWithLifecycle(initialValue = null)
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val deleteNoteMessage = stringResource(id = R.string.deleted_note)
	val archivedNoteMessage = stringResource(id = R.string.archived_note)
	val actionLabel = stringResource(id = R.string.undo)
	val noteSavedMessage = stringResource(id = R.string.note_saved)
	val noteUpdatedMessage = stringResource(id = R.string.note_updated)

	val selectedIds = rememberMutableStateList<Long>()
	val inSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }
	var showDeleteNotesDialog by rememberSaveable { mutableStateOf(value = false) }
	val isRecycleBinEmpty by remember { derivedStateOf { notes.isNullOrEmpty() } }

	LaunchedEffect(key1 = Unit) {
		when (noteAction) {
			NoteAction.DeleteEmptyNote -> {}
			NoteAction.InsertNote -> snackbarHostState.showSnackbar(noteSavedMessage)
			NoteAction.NoAction -> {}
			NoteAction.UpdateNote -> snackbarHostState.showSnackbar(noteUpdatedMessage)
		}
	}

	fun deleteNotes() {
		scope.launch {
			viewModel.deleteNotes(selectedIds)
			val selectedNotes = notes?.filter { note -> selectedIds.contains(note.id) }
			selectedNotes?.forEach { note -> MediaStorageManager.deleteItems(context, note.items) }
			showDeleteNotesDialog = false
			selectedIds.clear()
		}
	}

	fun emptyRecycleBin() {
		scope.launch {
			viewModel.emptyRecycleBin()
			notes?.forEach { note -> MediaStorageManager.deleteItems(context, note.items) }
		}
	}

	Scaffold(
		topBar = {
			if (inSelectionMode.not()) {
				if (screen.type == ScreenType.Main) {
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
				}
				else {
					TopAppBar(
						title = { Text(text = screen.title) },
						navigationIcon = {
							IconButton(onClick = onNavigationIconClick) {
								Icon(
									imageVector = Icons.Outlined.ArrowBack,
									contentDescription = ""
								)
							}
						},
						actions = {
							when (screen.type) {
								ScreenType.Tag -> {
									IconButton(onClick = {}) {
										Icon(
											imageVector = Icons.Outlined.Search,
											contentDescription = stringResource(id = R.string.search_notes)
										)
									}
									OverflowMenu(
										items = listOf(
											ActionItem(title = R.string.rename_tag, action = {}, icon = Icons.Outlined.Edit),
											ActionItem(title = R.string.delete_tag, action = {}, icon = Icons.Outlined.Delete)
										)
									)
								}
								ScreenType.Trash -> {
									if (isRecycleBinEmpty.not()) {
										IconButton(onClick = ::emptyRecycleBin) {
											Icon(
												imageVector = Icons.Outlined.DeleteForever,
												contentDescription = stringResource(id = R.string.empty_recycle_bin)
											)
										}
									}
								}
								else -> {
									IconButton(onClick = {}) {
										Icon(
											imageVector = Icons.Outlined.Search,
											contentDescription = stringResource(id = R.string.search_notes)
										)
									}
								}
							}
						}
					)
				}
			}
			else {
				TopAppBar(
					title = { Text(text = "${selectedIds.size}") },
					navigationIcon = {
						IconButton(onClick = { selectedIds.clear() }) {
							Icon(
								imageVector = Icons.Outlined.ArrowBack,
								contentDescription = stringResource(id = R.string.exit_multiselection_mode)
							)
						}
					},
					actions = {
						IconButton(onClick = { showDeleteNotesDialog = true }) {
							Icon(
								imageVector = Icons.Outlined.DeleteForever,
								contentDescription = stringResource(id = R.string.delete_notes)
							)
						}
					}
				)
			}
		},
		snackbarHost = { SnackbarHost(snackbarHostState) },
		floatingActionButton = {
			FloatingActionButton(onClick = onFabClick) {
				Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(id = R.string.add_note))
			}
		},
		floatingActionButtonPosition = FabPosition.End,
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
						EmptyView(
							icon = Icons.Outlined.Article,
							message = R.string.empty_notes_message
						)
					}
				}
			}
			else {
				notes?.let {
					NoteList(
						modifier = Modifier.padding(innerPadding),
						notes = it,
						selectedIds = selectedIds,
						inSelectionMode = inSelectionMode,
						onNoteClick = onItemClick,
						onNoteMovedToTrash = { note ->
							scope.launch {
								val noteMovedToTrash = viewModel.moveNotesToTrash(listOf(note))
								when (snackbarHostState.showSnackbar(
									message = deleteNoteMessage,
									actionLabel = actionLabel,
									duration = SnackbarDuration.Short
								)) {
									SnackbarResult.ActionPerformed -> viewModel.restoreNotesFromTrash(noteMovedToTrash)
									else -> {}
								}
							}
						},
						onNoteArchived = { note ->
							scope.launch {
								val archivedNote = viewModel.archiveNotes(listOf(note))
								when (snackbarHostState.showSnackbar(
									message = archivedNoteMessage,
									actionLabel = actionLabel,
									duration = SnackbarDuration.Short
								)) {
									SnackbarResult.ActionPerformed -> viewModel.restoreNotesArchived(archivedNote)
									else -> {}
								}
							}
						}
					)
				}
			}
			if (showDeleteNotesDialog) {
				DeleteNotesDialog(
					onDeleteNotes = ::deleteNotes,
					onDismiss = { showDeleteNotesDialog = false }
				)
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
			NoteList(modifier = Modifier.padding(innerPadding))
		}
	)
}

@DevicePreviews
@Composable
private fun NotesScreenPreview() = NotiesTheme { NotesScreen() }