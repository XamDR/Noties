package io.github.xamdr.noties.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.core.Note
import io.github.xamdr.noties.domain.EmptyTrashUseCase
import io.github.xamdr.noties.domain.GetTrashedNotesSyncUseCase
import io.github.xamdr.noties.domain.GetTrashedNotesUseCase
import io.github.xamdr.noties.domain.RestoreNoteUseCase
import io.github.xamdr.noties.ui.helpers.printDebug
import kotlinx.coroutines.launch
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
				restoreNoteUseCase(note.entity)
			}
		}
	}

	private companion object {
		private const val TAG = "TRASHED_NOTES"
	}
}