package io.github.xamdr.noties.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.GetAllNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetNotesUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
	private val getNotesUseCase: GetNotesUseCase,
	private val getAllNotesUseCase: GetAllNotesUseCase) : ViewModel() {

	fun getNotesByTag(tagName: String): LiveData<List<Note>> {
		return (if (tagName.isEmpty()) getAllNotesUseCase() else getNotesUseCase(tagName))
	}

//	fun moveNoteToTrash(note: DatabaseNoteEntity, action: (note: DatabaseNoteEntity) -> Unit) {
//		viewModelScope.launch {
//			moveNoteToTrashUseCase(note)
//			action(note)
//		}
//	}
//
//	fun restoreNote(note: DatabaseNoteEntity) {
//		viewModelScope.launch { restoreNoteUseCase(note) }
//	}

	fun deleteNotes(notes: List<Note>, action: () -> Unit) {
		viewModelScope.launch {
//			deleteNotesUseCase(notes)
//			action()
		}
	}

//	fun toggleLockedValueForNotes(notes: List<Note>, action: () -> Unit) {
//		val entities = notes.map { it.entity }
//
//		viewModelScope.launch {
//			if (entities.any { !it.isProtected }) {
//				lockNotesUseCase(entities)
//			}
//			else {
//				unlockNotesUseCase(entities)
//			}
//			action()
//		}
//	}
//
//	fun togglePinnedValueForNotes(notes: List<Note>, action: () -> Unit) {
//		val entities = notes.map { it.entity }
//
//		viewModelScope.launch {
//			if (entities.any { !it.isPinned }) {
//				pinNotesUseCase(entities)
//			}
//			else {
//				unpinNotesUseCase(entities)
//			}
//			action()
//		}
//	}

	fun moveNotes(notes: List<Note>, notebookId: Int, action: () -> Unit) {
		viewModelScope.launch {
//			moveNotesUseCase(notes.map { it.entity }, notebookId)
//			action()
		}
	}
}
