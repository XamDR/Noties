@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.xamdr.noties.ui.editor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toColor
import androidx.compose.material3.tokens.TopAppBarSmallTokens
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.components.OverflowMenu
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.ColorSaver
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.PermissionRationaleDialog
import io.github.xamdr.noties.ui.helpers.ShareHelper
import io.github.xamdr.noties.ui.helpers.clickableWithoutRipple
import io.github.xamdr.noties.ui.helpers.doActionOrRequestPermission
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import io.github.xamdr.noties.ui.helpers.showToast
import io.github.xamdr.noties.ui.media.ActionItem
import io.github.xamdr.noties.ui.media.MediaViewerActivity
import io.github.xamdr.noties.ui.reminders.AlarmManagerHelper
import io.github.xamdr.noties.ui.reminders.DateTimePickerDialog
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch
import java.time.Instant

@SuppressLint("InlinedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
	noteId: Long,
	text: String?,
	selectedTags: List<String>?,
	onNavigationIconClick: () -> Unit,
	onNavigatoToTags: (tags: List<String>) -> Unit,
	onNoteAction: (NoteAction, Long) -> Unit,
	onEditorColorChanged: (Color?) -> Unit,
	viewModel: EditorViewModel = hiltViewModel()
) {
	val context = LocalContext.current
	var titleInEditMode by rememberSaveable { mutableStateOf(value = false) }
	var openMenu by rememberSaveable { mutableStateOf(value = false) }
	val scope = rememberCoroutineScope()
	val modificationDate = DateTimeHelper.formatDateTime(
		if (viewModel.note.modificationDate == 0L) Instant.now().toEpochMilli() else viewModel.note.modificationDate
	)
	val snackbarHostState = remember { SnackbarHostState() }
	val noteEmpty by remember { derivedStateOf { viewModel.note.isEmpty() } }
	val errorOpenFile = stringResource(id = R.string.error_open_file)
	var showDateTimePicker by rememberSaveable { mutableStateOf(value = false) }
	var writeExternalStorageRationaleDialog by rememberSaveable { mutableStateOf(value = false) }
	var postNotificationRationaleDialog by rememberSaveable { mutableStateOf(value = false) }
	val permissionDeniedMessage = stringResource(id = R.string.permission_denied)
	var cameraUri by rememberSaveable { mutableStateOf<Uri?>(value = null) }
	var openColorSheet by rememberSaveable { mutableStateOf(value = false) }
	var containerColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(value = null) }

	fun openFile(uri: Uri?) {
		scope.launch {
			viewModel.readFileContent(uri, context) {
				this.launch { snackbarHostState.showSnackbar(errorOpenFile) }
			}
		}
	}

	val pickMediaLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.OpenMultipleDocuments(),
		onResult = viewModel::addItems
	)

	val mediaViewerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult(),
		onResult = {}
	)

	val openFileLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.OpenDocument(),
		onResult = ::openFile
	)

	val takePictureLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.TakePicture(),
		onResult = { success ->
			if (success && cameraUri != null) {
				val tempUri = cameraUri ?: throw IllegalArgumentException("$cameraUri is null")
				viewModel.addItems(listOf(tempUri))
			}
			else {
				context.showToast(R.string.error_take_picture)
			}
		}
	)

	fun takePicture() {
		cameraUri = MediaStorageManager.savePicture(context)
		takePictureLauncher.launch(cameraUri)
	}

	val writeExternalStorageAction = doActionOrRequestPermission(
		permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
		action = ::takePicture,
		requestPermission = { writeExternalStorageRationaleDialog = true },
		condition = { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q }
	)

	val postNotificationAction = doActionOrRequestPermission(
		permission = Manifest.permission.POST_NOTIFICATIONS,
		action = { showDateTimePicker = true },
		requestPermission = { postNotificationRationaleDialog = true },
		condition = { Build.VERSION.SDK_INT <= Build.VERSION_CODES.S },
	)

	LaunchedEffect(key1 = Unit) {
		launch {
			viewModel.getNote(noteId, text, selectedTags)
			containerColor = viewModel.note.color?.let { Color(it) }
			onEditorColorChanged(containerColor)
		}
	}

	BackHandler {
		scope.launch {
			val action = viewModel.saveNote(viewModel.note, noteId)
			when (action) {
				NoteAction.DeleteEmptyNote -> {}
				NoteAction.InsertNote -> AlarmManagerHelper.setAlarm(context, viewModel.note)
				NoteAction.NoAction -> {}
				NoteAction.UpdateNote -> AlarmManagerHelper.setAlarm(context, viewModel.note)
			}
			onNoteAction(action, viewModel.note.id)
		}
	}

	Scaffold(
		containerColor = containerColor ?: MaterialTheme.colorScheme.background,
		topBar = {
			TopAppBar(
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = containerColor ?: TopAppBarSmallTokens.ContainerColor.toColor()
				),
				title = {
					Text(
						text = viewModel.note.title.ifEmpty { stringResource(id = R.string.editor) },
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = if (viewModel.note.trashed) Modifier
							else Modifier.clickableWithoutRipple { titleInEditMode = titleInEditMode.not() }
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
						IconButton(onClick = { ShareHelper.shareContent(context, viewModel.note) }) {
							Icon(
								imageVector = Icons.Outlined.Share,
								contentDescription = stringResource(id = R.string.share_content)
							)
						}
					}
					OverflowMenu(
						items = if (viewModel.note.isTaskList && viewModel.note.trashed.not()) {
							listOf(
								ActionItem(
									title = R.string.hide_checkboxes,
									action = viewModel::exitTaskMode
								),
								ActionItem(
									title = R.string.check_all_checkboxes,
									action = { viewModel.markAllTasksAsDone(value = true) }
								),
								ActionItem(
									title = R.string.uncheck_all_checkboxes,
									action = { viewModel.markAllTasksAsDone(value = false) }
								)
							)
						}
						else if (viewModel.note.trashed) {
							listOf(
								ActionItem(
									title = R.string.restore_from_trash,
									action = viewModel::restoreNote,
									icon = Icons.Outlined.RestoreFromTrash
								)
							)
						}
						else {
							listOf(
								ActionItem(
									title = R.string.open_file,
									action = { openFileLauncher.launch(arrayOf(Constants.MIME_TYPE_TEXT)) },
									icon = Icons.Outlined.FileOpen
								)
							)
						}
					)
				}
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
		) {
			AnimatedVisibility(visible = titleInEditMode) {
				TextBox(
					placeholder = stringResource(id = R.string.editor),
					value = viewModel.note.title,
					onValueChange = viewModel::updateNoteTitle,
					modifier = Modifier.fillMaxWidth()
				)
			}
			Editor(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth(),
				note = viewModel.note,
				items = viewModel.items,
				tasks = viewModel.tasks,
				onNoteContentChange = viewModel::updateNoteContent,
				onItemCopied = viewModel::onItemCopied,
				onItemClick = { position ->
					navigateToMediaViewer(
						context = context,
						launcher = mediaViewerLauncher,
						items = viewModel.items.filterIsInstance<GridItem.Media>().map { it.data },
						position = position
					)
				},
				onDateTagClick = { showDateTimePicker = true },
				onTagClick = { onNavigatoToTags(viewModel.note.tags) },
				onTaskContentChanged = viewModel::updateTaskContent,
				onTaskDone = viewModel::setTaskStatus,
				onDragDropTask = viewModel::dragDropTask,
				onAddTask = viewModel::addTask,
				onRemoveTask = viewModel::removeTask
			)
			if (viewModel.note.trashed.not()) {
				EditorToolbar(
					onAddAttachmentIconClick = { openMenu = true },
					onPickColorIconClick = { openColorSheet = true },
					dateModified = modificationDate
				)
			}
		}
		if (openMenu) {
			EditorMenuBottomSheet(
				items = if (viewModel.note.isTaskList) EDITOR_MENU_ITEMS_TASK_MODE else EDITOR_MENU_ITEMS_NORMAL,
				sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
				onDismissRequest = { openMenu = false }
			) { item ->
				openMenu = false
				when (item.id) {
					R.id.gallery -> pickMediaLauncher.launch(
						arrayOf(Constants.MIME_TYPE_IMAGE, Constants.MIME_TYPE_VIDEO)
					)
					R.id.camera -> writeExternalStorageAction()
					R.id.tasks -> viewModel.enterTaskMode()
					R.id.reminder -> postNotificationAction()
					R.id.tags -> onNavigatoToTags(viewModel.note.tags)
				}
			}
		}
		if (showDateTimePicker) {
			DateTimePickerDialog(
				reminderDate = viewModel.note.reminderDate,
				onReminderDateSet = { dateTime ->
					viewModel.setReminder(dateTime)
					showDateTimePicker = false
				},
				onCancelReminder = {
					viewModel.cancelReminder(context)
					showDateTimePicker = false
				},
				onDismiss = { showDateTimePicker = false }
			)
		}
		if (writeExternalStorageRationaleDialog) {
			PermissionRationaleDialog(
				icon = Icons.Outlined.Folder,
				message = R.string.write_external_storage_permission_rationale,
				permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
				onPermissionGranted = ::takePicture,
				onPermissionDenied = {
					scope.launch { snackbarHostState.showSnackbar(permissionDeniedMessage) }
				},
				onDismiss = { writeExternalStorageRationaleDialog = false },
			)
		}
		if (postNotificationRationaleDialog) {
			PermissionRationaleDialog(
				icon = Icons.Outlined.Notifications,
				message = R.string.post_notifications_permission_rationale,
				permission = Manifest.permission.POST_NOTIFICATIONS,
				onPermissionGranted = { showDateTimePicker = true },
				onPermissionDenied = {
					scope.launch { snackbarHostState.showSnackbar(permissionDeniedMessage) }
				},
				onDismiss = { postNotificationRationaleDialog = false },
			)
		}
		if (openColorSheet) {
			val colors = if (isSystemInDarkTheme()) editorDarkColors else editorLightColors
			EditorColorBottomSheet(
				colors = colors,
				editorColor = containerColor,
				sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
				onColorSelected = { selectedColor ->
					containerColor = selectedColor
					viewModel.updateNoteColor(selectedColor)
					onEditorColorChanged(selectedColor)
				},
				onDismiss = { openColorSheet = false }
			)
		}
	}
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
				contentDescription = stringResource(id = R.string.gallery),
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
