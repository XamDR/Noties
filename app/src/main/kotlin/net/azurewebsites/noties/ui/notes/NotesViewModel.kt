package net.azurewebsites.noties.ui.notes

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.domain.Note
import net.azurewebsites.noties.data.AppRepository
import net.azurewebsites.noties.util.Empty
import net.azurewebsites.noties.util.SortMode

class NotesViewModel : ViewModel() {

	val text = MutableLiveData(String.Empty)

	private fun getNotes(directoryId: Int)
		= AppRepository.Instance.fetchNotes(directoryId).asLiveData()

	fun sortNotes(directoryId: Int, sortMode: SortMode): LiveData<List<Note>> = when (sortMode) {
		SortMode.Content -> getNotes(directoryId).map { result ->
			result.sortedBy { it.entity.text }
		}
		SortMode.LastEdit -> getNotes(directoryId).map { result ->
			result.sortedByDescending { it.entity.updateDate }
		}
		SortMode.Title -> getNotes(directoryId).map { result ->
			result.sortedBy { it.entity.title }
		}
	}

	fun deleteNotes(directoryId: Int, notes: List<Note>) {
		viewModelScope.launch {
			AppRepository.Instance.deleteNotes(directoryId, notes)
		}
	}

	fun restoreNote(directoryId: Int, note: Note, images: List<ImageEntity>) {
		viewModelScope.launch {
			AppRepository.Instance.insertNote(directoryId, note, images)
		}
	}

//	fun searchNotesByContent() = text.switchMap {
//		AppRepository.Instance.fetchNotesByContent(it).asLiveData()
//	}
}
