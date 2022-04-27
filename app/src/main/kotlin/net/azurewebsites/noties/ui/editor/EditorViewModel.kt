package net.azurewebsites.noties.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.domain.Note
import net.azurewebsites.noties.data.AppRepository

class EditorViewModel : ViewModel() {

	fun insertNote(directoryId: Int, note: Note, images: List<ImageEntity>) {
		viewModelScope.launch {
			AppRepository.Instance.insertNote(directoryId, note, images)
		}
	}

	fun updateNote(note: Note) {
		viewModelScope.launch {
			AppRepository.Instance.updateNote(note)
		}
	}

	fun deleteMediaItem(image: ImageEntity) {
		viewModelScope.launch {
			AppRepository.Instance.deleteMediaItem(image)
		}
	}

	fun updateMediaItem(image: ImageEntity) {
		viewModelScope.launch {
			AppRepository.Instance.updateMediaItem(image)
		}
	}
}
