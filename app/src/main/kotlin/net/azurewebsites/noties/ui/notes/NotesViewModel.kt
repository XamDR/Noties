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
	private val unlockNotesUseCase: UnlockNotesUseCase,
	private val pinNotesUseCase: PinNotesUseCase,
	private val unpinNotesUseCase: UnpinNotesUseCase,
	private val moveNotesUseCase: MoveNotesUseCase) : ViewModel() {

	fun sortNotes(notebookId: Int, sortMode: SortMode): LiveData<List<Note>> {
		val sortedNotes = when (sortMode) {
			SortMode.Content -> getNotesByFolderId(notebookId).map { result ->
				result.sortedWith(
					compareByDescending<Note> { it.entity.isPinned }
					.thenBy { it.entity.text }
				)
			}
			SortMode.LastEdit -> getNotesByFolderId(notebookId).map { result ->
				result.sortedWith(
					compareByDescending<Note> { it.entity.isPinned }
					.thenByDescending { it.entity.modificationDate }
				)
			}
			SortMode.Title -> getNotesByFolderId(notebookId).map { result ->
				result.sortedWith(
					compareByDescending<Note> { it.entity.isPinned }
					.thenBy { it.entity.title }
				)
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

	fun restoreNote(note: NoteEntity) {
		viewModelScope.launch { restoreNoteUseCase(note) }
	}

	fun deleteNotes(notes: List<Note>, action: () -> Unit) {
		viewModelScope.launch {
			deleteNotesUseCase(notes)
			action()
		}
	}

	fun toggleLockedValueForNotes(notes: List<Note>, action: () -> Unit) {
		val entities = notes.map { it.entity }

		viewModelScope.launch {
			if (entities.any { !it.isProtected }) {
				lockNotesUseCase(entities)
			}
			else {
				unlockNotesUseCase(entities)
			}
			action()
		}
	}

	fun togglePinnedValueForNotes(notes: List<Note>, action: () -> Unit) {
		val entities = notes.map { it.entity }

		viewModelScope.launch {
			if (entities.any { !it.isPinned }) {
				pinNotesUseCase(entities)
			}
			else {
				unpinNotesUseCase(entities)
			}
			action()
		}
	}

	fun moveNotes(notes: List<Note>, notebookId: Int, action: () -> Unit) {
		viewModelScope.launch {
			moveNotesUseCase(notes.map { it.entity }, notebookId)
			action()
		}
	}

	private fun getNotesByFolderId(notebookId: Int) =
		if (notebookId == 0) getAllNotesUseCase() else getNotesUseCase(notebookId)
}
