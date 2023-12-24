package io.github.xamdr.noties.ui.editor

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Task
import io.github.xamdr.noties.domain.model.UrlItem
import io.github.xamdr.noties.domain.model.containsItem
import io.github.xamdr.noties.domain.model.convertToString
import io.github.xamdr.noties.domain.model.joinToString
import io.github.xamdr.noties.domain.usecase.DeleteNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetNoteByIdUseCase
import io.github.xamdr.noties.domain.usecase.GetUrlsAsyncUseCase
import io.github.xamdr.noties.domain.usecase.InsertNoteUseCase
import io.github.xamdr.noties.domain.usecase.UpdateNoteUseCase
import io.github.xamdr.noties.ui.helpers.UriHelper
import io.github.xamdr.noties.ui.helpers.extractUrls
import io.github.xamdr.noties.ui.helpers.simpleName
import io.github.xamdr.noties.ui.reminders.AlarmManagerHelper
import timber.log.Timber
import java.io.FileNotFoundException
import java.time.Instant
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class EditorViewModel @Inject constructor(
	private val getNoteByIdUseCase: GetNoteByIdUseCase,
	private val insertNoteUseCase: InsertNoteUseCase,
	private val updateNoteUseCase: UpdateNoteUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val getUrlsAsyncUseCase: GetUrlsAsyncUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	var note by savedState.saveable { mutableStateOf(value = Note()) }
		private set

	val items = mutableStateListOf<GridItem>()
	val tasks = mutableStateListOf<Task>()
	val urls = mutableStateListOf<UrlItem>()

	fun updateNoteContent(text: String) {
		note = note.copy(text = text)
	}

	fun updateNoteTitle(title: String) {
		note = note.copy(title = title)
	}

	fun addItems(uris: List<Uri>) {
		if (uris.isEmpty()) return
		uris.forEach { uri -> items.add(GridItem.AndroidUri(src = uri)) }
	}

	fun onItemCopied(mediaItem: MediaItem, index: Int) {
		items[index] = GridItem.Media(data = mediaItem)
		if (items.all { it is GridItem.Media }) {
			val mediaItems = items
				.filterIsInstance<GridItem.Media>()
				.map { it.data }
				.filter { it.id == 0 && !note.items.contains(it) }
			note = note.copy(items = note.items + mediaItems)
		}
	}

	suspend fun readFileContent(uri: Uri?, context: Context, onFileError: () -> Unit) {
		if (uri != null) {
			try {
				val file = DocumentFile.fromSingleUri(context, uri)
				val text = UriHelper.readTextFromUri(context, uri)
				note = note.copy(title = file?.simpleName ?: String.Empty, text = text)
			}
			catch (e: FileNotFoundException) {
				Timber.e(e)
				onFileError()
			}
		}
	}

	suspend fun getNote(noteId: Long, text: String?, tags: List<String>?) {
		if (noteId != 0L && savedState.get<Note>("note") == null) {
			val noteDb = getNoteById(noteId)
			if (note.id != noteDb.id) {
				note = noteDb
			}
		}
		else if (noteId == 0L) {
			text?.let { note = note.copy(text = it) }
		}
		if (note.items.isNotEmpty()) {
			val itemsFromNote = note.items.map(GridItem::Media).filter { !items.contains(it) }
			items.addAll(itemsFromNote)
		}
		if (note.text.isNotEmpty()) {
			tasks.addAll(note.toTaskList().filter { !tasks.containsItem(it) })
		}
		if (tags != null && note.tags != tags) {
			note = note.copy(tags = tags)
		}
		if (note.urls.isNotEmpty()) {
			urls.addAll(getUrlsAsyncUseCase(note.urls))
		}
		Timber.d("Note: $note")
	}

	suspend fun saveNote(note: Note, noteId: Long): NoteAction {
		val urls = extractUrls(note.text)
		val finalNote = if (note.isTaskList) {
			note.copy(text = tasks.convertToString(), urls = urls)
		}
		else {
			note.copy(urls = urls)
		}
		Timber.d("Saved Note: $finalNote")
		return if (finalNote.id == 0L) insertNote(finalNote) else updateNote(finalNote, noteId)
	}

	fun setReminder(dateTime: Instant) {
		val value = dateTime.toEpochMilli()
		note = note.copy(reminderDate = value)
	}

	fun cancelReminder(context: Context) {
		note = note.copy(reminderDate = null)
		AlarmManagerHelper.cancelAlarm(context, note.id)
	}

	fun enterTaskMode() {
		tasks.addAll(note.toTaskList().filter { !tasks.containsItem(it) })
		note = note.copy(isTaskList = true)
	}

	fun exitTaskMode() {
		note = note.copy(text = tasks.joinToString(), isTaskList = false)
	}

	fun markAllTasksAsDone(value: Boolean) {
		tasks.replaceAll { (it as Task.Item).copy(done = value) }
	}

	fun updateTaskContent(index: Int, content: String) {
		val task = tasks[index] as Task.Item
		tasks[index] = task.copy(content = content)
	}

	fun setTaskStatus(index: Int, done: Boolean) {
		val task = tasks[index] as Task.Item
		tasks[index] = task.copy(done = done)
	}

	fun dragDropTask(from: Int, to: Int) {
		val fromIndex = from - items.size
		val toIndex = to - items.size
		if (toIndex in 0..< tasks.size && fromIndex in 0..< tasks.size) {
			tasks.add(toIndex, tasks.removeAt(fromIndex))
		}
	}

	fun addTask() {
		tasks.add(Task.Item())
	}

	fun removeTask(task: Task) {
		tasks.remove(task)
	}

	fun restoreNote() {
		note = note.copy(trashed = false)
	}

	fun updateNoteColor(color: Color?) {
		val newColor = color?.toArgb()
		note = note.copy(color = newColor)
	}

	private suspend fun getNoteById(noteId: Long) = getNoteByIdUseCase(noteId)

	private suspend fun insertNote(newNote: Note): NoteAction {
		return if (!newNote.isEmpty()) {
			val id = insertNoteUseCase(newNote)
			note = note.copy(id = id)
			NoteAction.InsertNote
		}
		else NoteAction.NoAction
	}

	private suspend fun updateNote(updatedNote: Note, noteId: Long): NoteAction {
		val originalNote = getNoteById(noteId)
		return if (updatedNote != originalNote) {
			if (updatedNote.isEmpty()) {
				deleteNotesUseCase(listOf(updatedNote.id))
				NoteAction.DeleteEmptyNote
			}
			else {
				updateNoteUseCase(updatedNote)
				NoteAction.UpdateNote
			}
		}
		else NoteAction.NoAction
	}
}