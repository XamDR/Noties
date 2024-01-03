package io.github.xamdr.noties.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import io.github.xamdr.noties.ui.components.ActionItem
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.theme.NotiesTheme
import io.github.xamdr.noties.ui.urls.UrlsBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
	screen: Screen,
	noteId: Long,
	query: String,
	onQueryChange: (String) -> Unit,
	onSearch: (String) -> Unit,
	active: Boolean,
	onActiveChange: (Boolean) -> Unit,
	onLeadingIconClick: () -> Unit,
	onNavigationIconClick: () -> Unit,
	onFabClick: () -> Unit,
	onItemClick: (Note) -> Unit,
	onRenameTag: () -> Unit,
	onDeleteTag: () -> Unit,
	noteAction: NoteAction,
	preferenceStorage: PreferenceStorage,
	searchContent: @Composable (ColumnScope.() -> Unit),
	viewModel: NotesViewModel = hiltViewModel()
) {
	val notes by viewModel.getNotes(screen).collectAsStateWithLifecycle(initialValue = null)
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val deleteNoteMessage = stringResource(id = R.string.deleted_note)
	val archivedNoteMessage = stringResource(id = R.string.archived_note)
	val unarchivedNoteMessage = stringResource(id = R.string.unarchived_note)
	val actionLabel = stringResource(id = R.string.undo)
	val noteSavedMessage = stringResource(id = R.string.note_saved)
	val noteUpdatedMessage = stringResource(id = R.string.note_updated)
	val emptyNoteDeletedMessage = stringResource(id = R.string.empty_note_deleted)
	val selectedIds = rememberMutableStateList<Long>()
	val inSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }
	val selectedNotes by remember {
		derivedStateOf { notes?.filter { note -> selectedIds.contains(note.id) }.orEmpty() }
	}
	var showDeleteNotesDialog by rememberSaveable { mutableStateOf(value = false) }
	val isRecycleBinEmpty by remember { derivedStateOf { notes.isNullOrEmpty() } }
	var isBatchDelete by rememberSaveable { mutableStateOf(value = false) }
	var layoutType by rememberSaveable { mutableStateOf(value = LayoutType.valueOf(preferenceStorage.layoutType)) }
	var showUrlsDialog by rememberSaveable { mutableStateOf(value = false) }
	val urls = rememberMutableStateList<String>()

	LaunchedEffect(key1 = Unit) {
		when (noteAction) {
			NoteAction.DeleteEmptyNote -> {
				viewModel.deleteNoteById(noteId)
				snackbarHostState.showSnackbar(emptyNoteDeletedMessage)
			}
			NoteAction.InsertNote -> {
				snackbarHostState.showSnackbar(noteSavedMessage)
				viewModel.saveUrls(noteId, viewModel.getNoteById(noteId).urls)
			}
			NoteAction.UpdateNote -> {
				snackbarHostState.showSnackbar(noteUpdatedMessage)
				viewModel.saveUrls(noteId, viewModel.getNoteById(noteId).urls)
			}
			NoteAction.NoAction -> {}
		}
	}

	fun deleteNotes() {
		scope.launch {
			if (selectedIds.isNotEmpty()) {
				viewModel.deleteNotes(selectedIds)
				selectedNotes.forEach { note -> MediaStorageManager.deleteItems(context, note.items) }
				selectedIds.clear()
			}
			else {
				viewModel.emptyRecycleBin()
				notes?.forEach { note -> MediaStorageManager.deleteItems(context, note.items) }
			}
			showDeleteNotesDialog = false
		}
	}

	fun restoreNotes() {
		scope.launch {
			if (selectedIds.isNotEmpty()) {
				viewModel.restoreNotesFromTrash(selectedNotes)
				selectedIds.clear()
			}
		}
	}

	fun togglePinnedValue() {
		scope.launch {
			viewModel.togglePinnedValue(selectedNotes)
			selectedIds.clear()
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
									contentDescription = stringResource(id = R.string.open_drawer)
								)
							}
						},
						trailingIcon = {
							IconButton(onClick = {
								when (layoutType) {
									LayoutType.Linear -> {
										layoutType = LayoutType.Grid
										preferenceStorage.layoutType = LayoutType.Grid.name
									}
									LayoutType.Grid -> {
										layoutType = LayoutType.Linear
										preferenceStorage.layoutType = LayoutType.Linear.name
									}
								}
							}) {
								Icon(
									imageVector = if (layoutType == LayoutType.Linear) Icons.Outlined.GridView else Icons.Outlined.ViewAgenda,
									contentDescription = stringResource(
										id = if (layoutType == LayoutType.Linear) R.string.grid_layout_view else R.string.linear_layout_view
									)
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
									imageVector = Icons.Outlined.Menu,
									contentDescription = stringResource(id = R.string.open_drawer)
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
											ActionItem(
												title = R.string.rename_tag,
												action = onRenameTag,
												icon = Icons.Outlined.Edit
											),
											ActionItem(
												title = R.string.delete_tag,
												action = onDeleteTag,
												icon = Icons.Outlined.Delete
											)
										)
									)
								}
								ScreenType.Trash -> {
									if (isRecycleBinEmpty.not()) {
										IconButton(
											onClick = {
												showDeleteNotesDialog = true
												isBatchDelete = false
											}
										) {
											Icon(
												imageVector = Icons.Outlined.DeleteForever,
												contentDescription = stringResource(id = R.string.empty_trash)
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
				val allPinned = selectedNotes.all { it.pinned }
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
						if (screen.type == ScreenType.Trash) {
							IconButton(onClick = ::restoreNotes) {
								Icon(
									imageVector = Icons.Outlined.RestoreFromTrash,
									contentDescription = stringResource(id = R.string.restore_from_trash)
								)
							}
						}
						else {
							IconButton(onClick = ::togglePinnedValue) {
								Icon(
									imageVector = if (allPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
									contentDescription = stringResource(id = if (allPinned) R.string.unpin_note else R.string.pin_note)
								)
							}
						}
						IconButton(
							onClick = {
								showDeleteNotesDialog = true
								isBatchDelete = true
							}
						) {
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
			if (screen.type != ScreenType.Archived && screen.type != ScreenType.Trash) {
				FloatingActionButton(onClick = onFabClick) {
					Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(id = R.string.add_note))
				}
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
					val gridItems = groupNotesByCondition(it, screen.type)
					NoteList(
						modifier = Modifier.padding(innerPadding),
						gridItems = gridItems,
						layoutType = layoutType,
						selectedIds = selectedIds,
						inSelectionMode = inSelectionMode,
						onNoteClick = onItemClick,
						onMoveNoteToTrash = { note ->
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
						onArchiveNote = { note ->
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
						},
						onUnarchiveNote = { note ->
							scope.launch {
								viewModel.restoreNotesArchived(listOf(note))
								snackbarHostState.showSnackbar(message = unarchivedNoteMessage)
							}
						},
						onUrlsTagClick = { sources ->
							urls.addAll(sources)
							showUrlsDialog = true
						}
					)
				}
			}
			if (showDeleteNotesDialog) {
				DeleteNotesDialog(
					isBatchDelete = isBatchDelete,
					onDeleteNotes = ::deleteNotes,
					onDismiss = { showDeleteNotesDialog = false }
				)
			}
			if (showUrlsDialog) {
				UrlsBottomSheet(
					sources = urls,
					sheetState = rememberModalBottomSheetState(),
					viewModel = viewModel,
					onDismiss = {
						urls.clear()
						showUrlsDialog = false
					}
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