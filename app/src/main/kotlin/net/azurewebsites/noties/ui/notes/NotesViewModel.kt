package net.azurewebsites.noties.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.domain.*
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
	private val getNotesUseCase: GetNotesUseCase,
	private val getAllNotesUseCase: GetAllNotesUseCase,
	private val moveNoteToTrashUseCase: MoveNoteToTrashUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val restoreNoteUseCase: RestoreNoteUseCase,
	private val lockNotesUseCase: LockNotesUseCase,
	private val unlockNotesUseCase: UnlockNotesUseCase) : ViewModel() {

	fun sortNotes(notebookId: Int, sortMode: SortMode): LiveData<List<Note>> {
		val sortedNotes = when (sortMode) {
			SortMode.Content -> getNotesByFolderId(notebookId).map {
				result -> result.sortedBy { it.entity.text }
			}
			SortMode.LastEdit -> getNotesByFolderId(notebookId).map {
				result -> result.sortedByDescending { it.entity.dateModification }
			}
			SortMode.Title -> getNotesByFolderId(notebookId).map {
				result -> result.sortedBy { it.entity.title }
			}
		}
		return sortedNotes.asLiveData()
	}

	fun moveNoteToTrash(note: NoteEntity, action: (note: NoteEntity) -> Unit) {
		viewModelScope.launch {
			moveNoteToTrashUseCase(note)
			action(note)
		}
	}

	fun restoreNote(note: NoteEntity, notebookId: Int) {
		viewModelScope.launch { restoreNoteUseCase(note, notebookId) }
	}

	fun deleteNotes(notes: List<Note>, action: () -> Unit) {
		viewModelScope.launch {
			deleteNotesUseCase(notes)
			action()
		}
	}

	fun lockNotes(notes: List<Note>) {
		viewModelScope.launch {
			lockNotesUseCase(notes.map { it.entity })
		}
	}

	fun unlockNotes(notes: List<Note>) {
		viewModelScope.launch {
			unlockNotesUseCase(notes.map { it.entity })
		}
	}

	private fun getNotesByFolderId(notebookId: Int) =
		if (notebookId == 0) getAllNotesUseCase() else getNotesUseCase(notebookId)
}
