package net.azurewebsites.noties.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.azurewebsites.noties.data.ImageDao
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.domain.Note
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(private val imageDao: ImageDao) : ViewModel() {

	fun insertNote(directoryId: Int, note: Note, images: List<ImageEntity>) {
		viewModelScope.launch {
			imageDao.insertNoteWithMediaItems(directoryId, note, images)
		}
	}

	fun updateNote(note: Note) {
		viewModelScope.launch {
			imageDao.updateNote(note)
		}
	}

	fun deleteMediaItem(image: ImageEntity) {
		viewModelScope.launch {
			imageDao.deleteMediaItem(image)
		}
	}

	fun updateMediaItem(image: ImageEntity) {
		viewModelScope.launch {
			imageDao.updateMediaItem(image)
		}
	}
}
