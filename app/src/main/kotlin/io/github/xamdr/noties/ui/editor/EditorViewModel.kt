package io.github.xamdr.noties.ui.editor

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.DeleteNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetNoteByIdUseCase
import io.github.xamdr.noties.domain.usecase.InsertNoteUseCase
import io.github.xamdr.noties.domain.usecase.UpdateNoteUseCase
import io.github.xamdr.noties.ui.helpers.UriHelper
import io.github.xamdr.noties.ui.helpers.simpleName
import timber.log.Timber
import java.io.FileNotFoundException
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class EditorViewModel @Inject constructor(
	private val getNoteByIdUseCase: GetNoteByIdUseCase,
	private val insertNoteUseCase: InsertNoteUseCase,
	private val updateNoteUseCase: UpdateNoteUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	var note by savedState.saveable { mutableStateOf(value = Note()) }
		private set

	val items = mutableStateListOf<GridItem>()

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

	suspend fun getNote(noteId: Long, tags: List<String>?) {
		if (noteId != 0L && savedState.get<Note>("note") == null) {
			note = getNoteById(noteId)
		}
		if (note.items.isNotEmpty()) {
			val itemsFromNote = note.items.map(GridItem::Media).filter { !items.contains(it) }
			items.addAll(itemsFromNote)
		}
		if (tags != null && note.tags != tags) {
			note = note.copy(tags = tags)
		}
		Timber.d("Note: $note")
	}

	suspend fun saveNote(note: Note, noteId: Long): NoteAction {
		Timber.d("Saved Note: $note")
		return if (note.id == 0L) insertNote(note) else updateNote(note, noteId)
	}

	private suspend fun getNoteById(noteId: Long): Note = getNoteByIdUseCase(noteId)

	private suspend fun insertNote(note: Note): NoteAction {
		return if (!note.isEmpty()) {
			insertNoteUseCase(note)
			NoteAction.InsertNote
		}
		else NoteAction.NoAction
	}

	private suspend fun updateNote(note: Note, noteId: Long): NoteAction {
		val originalNote = getNoteById(noteId)
		return if (note != originalNote) {
			if (note.isEmpty()) {
				deleteNotesUseCase(listOf(note.id))
				NoteAction.DeleteEmptyNote
			}
			else {
				updateNoteUseCase(note)
				NoteAction.UpdateNote
			}
		}
		else NoteAction.NoAction
	}
}