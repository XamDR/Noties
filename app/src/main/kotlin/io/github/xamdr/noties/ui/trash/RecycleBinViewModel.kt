package io.github.xamdr.noties.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Note
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor() : ViewModel() {

	var notes: List<Note> = emptyList()

	init {
		viewModelScope.launch {
//			notes = getTrashedNotesSyncUseCase()
//			printDebug(TAG, notes)
		}
	}

	fun getTrashedNotes() {
//		getTrashedNotesUseCase().asLiveData()
	}

	fun emptyRecycleBin(action: () -> Unit) {
		viewModelScope.launch {
//			emptyTrashUseCase()
//			action()
		}
	}

	fun restoreNotes(notes: List<Note>) {
		viewModelScope.launch {
//			for (note in notes) {
//				restoreNoteUseCase(note.entity)
//			}
		}
	}

	private companion object {
		private const val TAG = "TRASHED_NOTES"
	}
}