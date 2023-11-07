package io.github.xamdr.noties.ui.editor

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.components.OverflowMenu
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.ShareHelper
import io.github.xamdr.noties.ui.helpers.UriHelper
import io.github.xamdr.noties.ui.helpers.clickableWithoutRipple
import io.github.xamdr.noties.ui.helpers.simpleName
import io.github.xamdr.noties.ui.media.ActionItem
import io.github.xamdr.noties.ui.media.MediaViewerActivity
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.FileNotFoundException
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
	noteId: Long,
	onNavigationIconClick: () -> Unit,
	onNavigatoToTags: () -> Unit,
	onNoteAction: (NoteAction) -> Unit,
	viewModel: EditorViewModel = hiltViewModel()
) {
	var note by remember { mutableStateOf(Note()) }
	val context = LocalContext.current
	var titleInEditMode by rememberSaveable { mutableStateOf(value = false) }
	var openMenu by rememberSaveable { mutableStateOf(value = false) }
	val scope = rememberCoroutineScope()
	val items = remember { mutableStateListOf<GridItem>() }
	val modificationDate = if (note.modificationDate == 0L) DateTimeHelper.formatDateTime(Instant.now().toEpochMilli())
		else DateTimeHelper.formatDateTime(note.modificationDate)
	val snackbarHostState = remember { SnackbarHostState() }
	val noteEmpty by remember { derivedStateOf { note.isEmpty() } }
	val errorOpenFile = stringResource(id = R.string.error_open_file)

	fun addItems(uris: List<Uri>) {
		if (uris.isEmpty()) return
		uris.forEach { uri -> items.add(GridItem.AndroidUri(src = uri)) }
	}

	fun onItemCopied(mediaItem: MediaItem, index: Int) {
		items[index] = GridItem.Media(data = mediaItem)
		if (items.all { it is GridItem.Media }) {
			note = addMediaItems(note, items)
		}
	}

	fun openFile(uri: Uri?) {
		scope.launch {
			readFileContent(
				uri = uri,
				context = context,
				onFileSuccess = { note = it },
				onFileError = { this.launch { snackbarHostState.showSnackbar(errorOpenFile) } }
			)
		}
	}

	val pickMediaLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.OpenMultipleDocuments(),
		onResult = ::addItems
	)

	val mediaViewerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult(),
		onResult = {}
	)

	val openFileLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.OpenDocument(),
		onResult = ::openFile
	)

	LaunchedEffect(key1 = Unit) {
		scope.launch {
			note = viewModel.getNote(noteId)
			items.addAll(note.items.map(GridItem::Media))
		}
	}

	BackHandler {
		scope.launch { onNoteAction(viewModel.saveNote(note, noteId)) }
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = note.title.ifEmpty { stringResource(id = R.string.editor) },
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.clickableWithoutRipple { titleInEditMode = titleInEditMode.not() }
					)
				},
				navigationIcon = {
					IconButton(onClick = {
						if (titleInEditMode) titleInEditMode = false else onNavigationIconClick()
					}) {
						Icon(
							imageVector = if (titleInEditMode) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.back_button_description)
						)
					}
				},
				actions = {
					if (noteEmpty.not()) {
						IconButton(onClick = { ShareHelper.shareContent(context, note) }) {
							Icon(
								imageVector = Icons.Outlined.Share,
								contentDescription = stringResource(id = R.string.share_content)
							)
						}
					}
					OverflowMenu(
						items = listOf(
							ActionItem(
								title = R.string.open_file,
								action = { openFileLauncher.launch(arrayOf(Constants.MIME_TYPE_TEXT)) },
								icon = Icons.Outlined.FileOpen
							),
							ActionItem(
								title = R.string.add_tags,
								action = onNavigatoToTags,
								icon = Icons.Outlined.NewLabel
							),
						)
					)
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
					items = items,
					onNoteContentChange = { text -> note = note.copy(text = text) },
					onItemCopied = ::onItemCopied,
					onItemClick = { position ->
						navigateToMediaViewer(context, mediaViewerLauncher, getMediaItems(items), position)
					}
				)
				EditorToolbar(
					onAddAttachmentIconClick = { openMenu = true },
					onPickColorIconClick = {},
					dateModified = modificationDate
				)
			}
			if (openMenu) {
				EditorMenuBottomSheet(
					sheetState = SheetState(skipPartiallyExpanded = true),
					onDismissRequest = { openMenu = false }
				) { item ->
					openMenu = false
					when (item.id) {
						R.id.attach_media -> pickMediaLauncher.launch(
							arrayOf(Constants.MIME_TYPE_IMAGE, Constants.MIME_TYPE_VIDEO)
						)
						R.id.take_picture -> {} //takePictureLauncher.launch()
					}
				}
			}
		}
	)
}

private fun getMediaItems(items: SnapshotStateList<GridItem>): List<MediaItem> {
	return items.filterIsInstance<GridItem.Media>().map { it.data }
}

private fun addMediaItems(note: Note, items: SnapshotStateList<GridItem>): Note {
	val mediaItems = getMediaItems(items).filter { it.id == 0 }
	return note.copy(items = note.items + mediaItems)
}

private fun navigateToMediaViewer(
	context: Context,
	launcher: ActivityResultLauncher<Intent>,
	items: List<MediaItem>,
	position: Int
) {
	val intent = Intent(context, MediaViewerActivity::class.java).apply {
		putExtra(Constants.BUNDLE_ITEMS, ArrayList(items))
		putExtra(Constants.BUNDLE_POSITION, position)
	}
	launcher.launch(intent)
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

private suspend fun readFileContent(
	uri: Uri?,
	context: Context,
	onFileSuccess: (Note) -> Unit,
	onFileError: () -> Unit
): Note? {
	if (uri != null) {
		try {
			val file = DocumentFile.fromSingleUri(context, uri)
			val text = UriHelper.readTextFromUri(context, uri)
			return Note(title = file?.simpleName ?: String.Empty, text = text).also {
				onFileSuccess(it)
			}
		}
		catch (e: FileNotFoundException) {
			Timber.e(e)
			onFileError()
		}
	}
	return null
}