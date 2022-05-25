package net.azurewebsites.noties.ui.notes

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.domain.GetNotesUseCase
import net.azurewebsites.noties.domain.MoveNoteToTrashUseCase
import net.azurewebsites.noties.domain.RestoreNoteUseCase
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
	private val getNotesUseCase: GetNotesUseCase,
	private val moveNoteToTrashUseCase: MoveNoteToTrashUseCase,
	private val restoreNoteUseCase: RestoreNoteUseCase) : ViewModel() {

	val text = MutableLiveData(String.Empty)

	private fun getNotes(folderId: Int) = getNotesUseCase(folderId)

	fun sortNotes(folderId: Int, sortMode: SortMode): LiveData<List<Note>> {
		val sortedNotes = when (sortMode) {
			SortMode.Content -> getNotes(folderId).map { result -> result.sortedBy { it.entity.text } }
			SortMode.LastEdit -> getNotes(folderId).map { result -> result.sortedByDescending { it.entity.dateModification } }
			SortMode.Title -> getNotes(folderId).map { result -> result.sortedBy { it.entity.title } }
		}
		return sortedNotes.asLiveData()
	}

	fun moveNoteToTrash(note: NoteEntity) {
		viewModelScope.launch { moveNoteToTrashUseCase(note) }
	}

	fun restoreNote(note: NoteEntity, folderId: Int) {
		viewModelScope.launch { restoreNoteUseCase(note, folderId) }
	}
}
