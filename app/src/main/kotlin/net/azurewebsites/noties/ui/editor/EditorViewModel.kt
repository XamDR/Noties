package net.azurewebsites.noties.ui.editor

import android.content.Context
import android.net.Uri
import android.text.Editable
import androidx.core.content.FileProvider
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
import net.azurewebsites.noties.domain.DeleteImagesUseCase
import net.azurewebsites.noties.domain.InsertNoteUseCase
import net.azurewebsites.noties.domain.UpdateImageUseCase
import net.azurewebsites.noties.domain.UpdateNoteUseCase
import net.azurewebsites.noties.ui.helpers.getUriExtension
import net.azurewebsites.noties.ui.helpers.getUriMimeType
import net.azurewebsites.noties.ui.image.AltTextState
import net.azurewebsites.noties.ui.image.ImageStorageManager
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val insertNoteUseCase: InsertNoteUseCase,
	private val updateNoteUseCase: UpdateNoteUseCase,
	private val updateImageUseCase: UpdateImageUseCase,
	private val deleteImagesUseCase: DeleteImagesUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	var note = savedState[NOTE] ?: Note()

	val tempNote = savedState[TEMP_NOTE] ?: note.clone()

	private val _description = MutableStateFlow(String.Empty)
	val description = _description.asStateFlow()

	private val state: MutableStateFlow<AltTextState> = MutableStateFlow(AltTextState.EmptyDescription)
	val altTextState = state.asLiveData()

	suspend fun addImages(context: Context, uris: List<Uri>) {
		for (uri in uris) {
			val newUri = copyUri(context, uri)
			val image = ImageEntity(
				uri = newUri,
				mimeType = context.getUriMimeType(newUri),
				noteId = note.entity.id
			)
			note.images += image
		}
	}

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

	private suspend fun copyUri(context: Context, uri: Uri): Uri {
		val extension = context.getUriExtension(uri) ?: DEFAULT_IMAGE_EXTENSION
		val fileName = buildString {
			append("IMG_")
			append(DateTimeFormatter.ofPattern(PATTERN).format(LocalDateTime.now()))
			append("_${(0..999).random()}.$extension")
		}
		val fullPath = ImageStorageManager.saveImage(context, uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(context, AUTHORITY, file)
	}

	private companion object {
		private const val AUTHORITY = "net.azurewebsites.noties"
		private const val DEFAULT_IMAGE_EXTENSION = "jpeg"
		private const val PATTERN = "yyyyMMdd_HHmmss"
		private const val NOTE = "note"
		private const val TEMP_NOTE = "temp_note"
	}
}