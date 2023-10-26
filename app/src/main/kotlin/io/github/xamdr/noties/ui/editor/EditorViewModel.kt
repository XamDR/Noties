package io.github.xamdr.noties.ui.editor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.*
import io.github.xamdr.noties.ui.helpers.Constants
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val getNoteByIdUseCase: GetNoteByIdUseCase,
	private val insertNoteUseCase: InsertNoteUseCase,
	private val updateNoteUseCase: UpdateNoteUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val deleteMediaItemsUseCase: DeleteMediaItemsUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	val isTaskList = MutableLiveData(false)

	suspend fun getNote(noteId: Long) = savedState.get<Note>(Constants.BUNDLE_NOTE)
		?: (if (noteId == 0L) Note() else getNoteById(noteId))

	suspend fun saveNote(note: Note, noteId: Long) =
		if (note.id == 0L) insertNote(note) else updateNote(note, noteId)

	suspend fun deleteItems(items: List<MediaItem>) = deleteMediaItemsUseCase(items)

	fun saveState(note: Note) {
		savedState[Constants.BUNDLE_NOTE] = note
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