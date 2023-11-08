package io.github.xamdr.noties.ui.editor

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val getNoteByIdUseCase: GetNoteByIdUseCase,
	private val insertNoteUseCase: InsertNoteUseCase,
	private val updateNoteUseCase: UpdateNoteUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	savedState: SavedStateHandle) : ViewModel() {

	@OptIn(SavedStateHandleSaveableApi::class)
	var note by savedState.saveable { mutableStateOf(value = Note()) }
		private set

	fun updateNoteContent(text: String) {
		note = note.copy(text = text)
	}

	fun updateNoteTitle(title: String) {
		note = note.copy(title = title)
	}

	fun addMediaItems(items: SnapshotStateList<GridItem>) {
		val mediaItems = items
			.filterIsInstance<GridItem.Media>()
			.map { it.data }
			.filter { it.id == 0 }
		note = note.copy(items = note.items + mediaItems)
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

	suspend fun getNote(noteId: Long) {
		note = getNoteById(noteId)
	}

	suspend fun saveNote(note: Note, noteId: Long) =
		if (note.id == 0L) insertNote(note) else updateNote(note, noteId)

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