package net.azurewebsites.eznotes.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.core.Note
import net.azurewebsites.eznotes.data.AppRepository

class TextEditorViewModel : ViewModel() {

	fun insertNote(directoryId: Int, note: Note, mediaItems: List<MediaItemEntity>) {
		viewModelScope.launch {
			AppRepository.Instance.insertNote(directoryId, note, mediaItems)
		}
	}

	fun updateNote(note: Note) {
		viewModelScope.launch {
			AppRepository.Instance.updateNote(note)
		}
	}

	fun deleteMediaItem(mediaItem: MediaItemEntity) {
		viewModelScope.launch {
			AppRepository.Instance.deleteMediaItem(mediaItem)
		}
	}

	fun updateMediaItem(mediaItem: MediaItemEntity) {
		viewModelScope.launch {
			AppRepository.Instance.updateMediaItem(mediaItem)
		}
	}
}
