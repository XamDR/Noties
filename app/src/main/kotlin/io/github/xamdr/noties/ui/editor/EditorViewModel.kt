package io.github.xamdr.noties.ui.editor

import android.text.Editable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.DeleteImagesUseCase
import io.github.xamdr.noties.domain.usecase.InsertNoteUseCase
import io.github.xamdr.noties.domain.usecase.UpdateImageUseCase
import io.github.xamdr.noties.ui.image.AltTextState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val insertNoteUseCase: InsertNoteUseCase,
	private val updateImageUseCase: UpdateImageUseCase,
	private val deleteImagesUseCase: DeleteImagesUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	var note = savedState[NOTE] ?: Note()
//
//	val tempNote = savedState[TEMP_NOTE] ?: note.clone()
//
//	val entity get() = note.entity

	private val _description = MutableStateFlow(String.Empty)
	val description = _description.asStateFlow()

	private val state: MutableStateFlow<AltTextState> = MutableStateFlow(AltTextState.EmptyDescription)
	val altTextState = state.asLiveData()

	fun deleteImages(images: List<Image>) {
		viewModelScope.launch {
			deleteImagesUseCase(images)
		}
	}

	fun updateImageAltText(s: Editable) {
		_description.update { s.toString() }
		if (_description.value.isNotEmpty()) {
			state.update { AltTextState.EditingDescription }
		}
		else {
			state.update { AltTextState.EmptyDescription }
		}
	}

	fun updateImage(image: Image, description: String, action: () -> Unit) {
		viewModelScope.launch {
			updateImageUseCase(image, description)
			action()
		}
	}

	fun saveState() {
//		savedState[NOTE] = note
//		savedState[TEMP_NOTE] = tempNote
	}

	fun insertNote(note: Note, action: () -> Unit) {
		viewModelScope.launch {
			insertNoteUseCase(note, note.images)
			action()
		}
	}

//	fun updateNote(note: Note, action: () -> Unit) {
//		viewModelScope.launch {
//			updateNoteUseCase(note.entity, note.images)
//			action()
//		}
//	}

	fun updateNote(newValue: Note) {
//		note.entity = newValue
	}

	companion object {
		const val NOTE = "note"
		private const val TEMP_NOTE = "temp_note"
	}
}