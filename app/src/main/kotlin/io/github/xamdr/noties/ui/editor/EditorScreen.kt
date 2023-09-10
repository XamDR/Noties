package io.github.xamdr.noties.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
	onNavigationIconClick: () -> Unit,
	noteId: Long,
	onBack: () -> Unit,
	viewModel: EditorViewModel = hiltViewModel()
) {
	var titleInEditMode by rememberSaveable { mutableStateOf(false) }
	var note by remember { mutableStateOf(Note()) }
	val snackbarHostState = remember { SnackbarHostState() }
	val noteSavedMessage = stringResource(id = R.string.note_saved)
	val noteUpdatedMessage = stringResource(id = R.string.note_updated)
	val emptyNoteDeletedMessage = stringResource(id = R.string.empty_note_deleted)
	val coroutineScope = rememberCoroutineScope()

	LaunchedEffect(key1 = Unit) {
		coroutineScope.launch {
			note = viewModel.getNote(noteId)
		}
	}
	BackHandler {
		coroutineScope.launch {
			when (viewModel.saveNote(note, noteId)) {
				NoteAction.DeleteEmptyNote -> snackbarHostState.showSnackbar(emptyNoteDeletedMessage)
				NoteAction.InsertNote -> snackbarHostState.showSnackbar(noteSavedMessage)
				NoteAction.NoAction -> {}
				NoteAction.UpdateNote -> snackbarHostState.showSnackbar(noteUpdatedMessage)
			}
			onBack()
		}
	}
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = stringResource(id = R.string.editor),
						modifier = Modifier.clickable { titleInEditMode = titleInEditMode.not() }
					)
				},
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
		snackbarHost = { SnackbarHost(snackbarHostState) },
		content = { innerPadding ->
			Column(
				modifier = Modifier
					.padding(innerPadding)
					.fillMaxSize()
			) {
				AnimatedVisibility(visible = titleInEditMode) {
					TextBox(
						placeholder = stringResource(id = R.string.editor),
						value = note.title,
						onValueChange = { title -> note = note.copy(title = title) },
						modifier = Modifier.fillMaxWidth()
					)
				}
				Editor(
					modifier = Modifier
						.weight(1f)
						.fillMaxWidth(),
					note = note,
					onNoteContentChange = { text -> note = note.copy(text = text) }
				)
				EditorToolbar(
					onAddAttachmentIconClick = {},
					onPickColorIconClick = {},
					dateModified = String.Empty
				)
			}
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen() {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = stringResource(id = R.string.editor)) },
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
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {
				Editor(
					modifier = Modifier
						.weight(1f)
						.fillMaxWidth()
				)
				EditorToolbar(
					onAddAttachmentIconClick = {},
					onPickColorIconClick = {},
					dateModified = "Última edición: 14/12/2021 22:20"
				)
			}
		}
	)
}

@DevicePreviews
@Composable
private fun EditorScreenPreview() = NotiesTheme { EditorScreen() }

@Composable
private fun EditorToolbar(
	onAddAttachmentIconClick: () -> Unit,
	onPickColorIconClick: () -> Unit,
	dateModified: String
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		IconButton(onClick = onAddAttachmentIconClick) {
			Icon(
				imageVector = Icons.Outlined.AddBox,
				contentDescription = stringResource(id = R.string.add_attachment),
				modifier = Modifier.padding(start = 4.dp)
			)
		}
		Text(
			text = dateModified,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f),
			textAlign = TextAlign.Center
		)
		IconButton(onClick = onPickColorIconClick) {
			Icon(
				imageVector = Icons.Outlined.Palette,
				contentDescription = stringResource(id = R.string.pick_note_color),
				modifier = Modifier.padding(end = 4.dp)
			)
		}
	}
}