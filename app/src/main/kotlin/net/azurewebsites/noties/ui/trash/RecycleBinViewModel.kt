package net.azurewebsites.noties.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.domain.EmptyTrashUseCase
import net.azurewebsites.noties.domain.GetTrashedNotesSyncUseCase
import net.azurewebsites.noties.domain.GetTrashedNotesUseCase
import net.azurewebsites.noties.domain.RestoreNoteUseCase
import net.azurewebsites.noties.ui.helpers.printDebug
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
	private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
	private val getTrashedNotesSyncUseCase: GetTrashedNotesSyncUseCase,
	private val emptyTrashUseCase: EmptyTrashUseCase,
	private val restoreNoteUseCase: RestoreNoteUseCase) : ViewModel() {

	var notes: List<Note> = emptyList()

	init {
		viewModelScope.launch {
			notes = getTrashedNotesSyncUseCase()
			printDebug(TAG, notes)
		}
	}

	fun getTrashedNotes() = getTrashedNotesUseCase().asLiveData()

	fun emptyRecycleBin(action: () -> Unit) {
		viewModelScope.launch {
			emptyTrashUseCase()
			action()
		}
	}

	fun restoreNotes(notes: List<Note>) {
		viewModelScope.launch {
			for (note in notes) {
				// How to get the previous notebookId, current one == -1
				restoreNoteUseCase(note.entity, 0)
			}
		}
	}

	private companion object {
		private const val TAG = "TRASHED_NOTES"
	}
}