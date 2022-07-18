package net.azurewebsites.noties.ui.editor

import android.text.Editable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.domain.DeleteImagesUseCase
import net.azurewebsites.noties.domain.InsertNoteUseCase
import net.azurewebsites.noties.domain.UpdateImageUseCase
import net.azurewebsites.noties.domain.UpdateNoteUseCase
import net.azurewebsites.noties.ui.image.AltTextState
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val insertNoteUseCase: InsertNoteUseCase,
	private val updateNoteUseCase: UpdateNoteUseCase,
	private val updateImageUseCase: UpdateImageUseCase,
	private val deleteImagesUseCase: DeleteImagesUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	val note = savedState[NOTE] ?: Note()

	val tempNote = savedState[TEMP_NOTE] ?: note.clone()

	val entity get() = note.entity

	private val _description = MutableStateFlow(String.Empty)
	val description = _description.asStateFlow()

	private val state: MutableStateFlow<AltTextState> = MutableStateFlow(AltTextState.EmptyDescription)
	val altTextState = state.asLiveData()

	fun deleteImages(images: List<ImageEntity>) {
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

	fun updateImage(image: ImageEntity, description: String, action: () -> Unit) {
		viewModelScope.launch {
			updateImageUseCase(image, description)
			action()
		}
	}

	fun saveState() {
		savedState[NOTE] = note
		savedState[TEMP_NOTE] = tempNote
	}

	fun insertNote(note: Note, action: () -> Unit) {
		viewModelScope.launch {
			insertNoteUseCase(note.entity, note.images)
			action()
		}
	}

	fun updateNote(note: Note, action: () -> Unit) {
		viewModelScope.launch {
			updateNoteUseCase(note.entity, note.images)
			action()
		}
	}

	fun updateNote(newValue: NoteEntity) {
		note.entity = newValue
	}

	companion object {
		const val NOTE = "note"
		private const val TEMP_NOTE = "temp_note"
	}
}