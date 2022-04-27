package net.azurewebsites.eznotes.ui.notes

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.core.Note
import net.azurewebsites.eznotes.data.AppRepository
import net.azurewebsites.eznotes.util.Empty
import net.azurewebsites.eznotes.util.SortMode

class NoteListViewModel : ViewModel() {

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

	fun restoreNote(directoryId: Int, note: Note, mediaItems: List<MediaItemEntity>) {
		viewModelScope.launch {
			AppRepository.Instance.insertNote(directoryId, note, mediaItems)
		}
	}

//	fun searchNotesByContent() = text.switchMap {
//		AppRepository.Instance.fetchNotesByContent(it).asLiveData()
//	}
}
