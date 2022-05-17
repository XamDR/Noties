package net.azurewebsites.noties.ui.notes

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.azurewebsites.noties.data.ImageDao
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(private val imageDao: ImageDao) : ViewModel() {

	val text = MutableLiveData(String.Empty)

	private fun getNotes(directoryId: Int)
		= imageDao.getNoteWithMediaItems(directoryId).asLiveData()

	fun sortNotes(directoryId: Int, sortMode: SortMode): LiveData<List<Note>> = when (sortMode) {
		SortMode.Content -> getNotes(directoryId).map { result -> result.sortedBy { it.entity.text } }
		SortMode.LastEdit -> getNotes(directoryId).map { result -> result.sortedByDescending { it.entity.dateModification } }
		SortMode.Title -> getNotes(directoryId).map { result -> result.sortedBy { it.entity.title } }
	}

	fun deleteNotes(directoryId: Int, notes: List<Note>) {
		viewModelScope.launch {
			imageDao.deleteNotes(directoryId, notes)
		}
	}

	fun restoreNote(directoryId: Int, note: Note, images: List<ImageEntity>) {
		viewModelScope.launch {
			imageDao.insertNoteWithMediaItems(directoryId, note, images)
		}
	}
}
