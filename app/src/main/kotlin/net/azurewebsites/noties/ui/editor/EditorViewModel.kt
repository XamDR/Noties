package net.azurewebsites.noties.ui.editor

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.domain.DeleteImagesUseCase
import net.azurewebsites.noties.domain.InsertNoteWithImagesUseCase
import net.azurewebsites.noties.domain.UpdateImageUseCase
import net.azurewebsites.noties.domain.UpdateNoteUseCase
import net.azurewebsites.noties.ui.helpers.extractUrls
import net.azurewebsites.noties.ui.helpers.getUriExtension
import net.azurewebsites.noties.ui.helpers.getUriMimeType
import net.azurewebsites.noties.ui.image.ImageStorageManager
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val insertNoteWithImagesUseCase: InsertNoteWithImagesUseCase,
	private val updateNoteUseCase: UpdateNoteUseCase,
	private val updateImageUseCase: UpdateImageUseCase,
	private val deleteImagesUseCase: DeleteImagesUseCase) : ViewModel() {

	val note = MutableStateFlow(Note())
	val tempNote = MutableStateFlow(Note())
	val images get() = note.map { it.images }.asLiveData()
	val description = MutableStateFlow(String.Empty)

	suspend fun addImages(context: Context, uris: List<Uri>) {
		for (uri in uris) {
			val newUri = copyUri(context, uri)
			val image = ImageEntity(
				uri = newUri,
				mimeType = context.getUriMimeType(newUri),
				noteId = note.value.entity.id
			)
			note.value.images += image
		}
	}

	fun deleteImages(images: List<ImageEntity>) {
		viewModelScope.launch {
			deleteImagesUseCase(images)
		}
	}

	fun updateImage(image: ImageEntity, description: String) {
		viewModelScope.launch {
			updateImageUseCase(image, description)
		}
	}

	suspend fun insertorUpdateNote(folderId: Int): Result? {
		if (note.value.isNonEmpty()) {
			if (note.value.entity.id == 0L) {
				val newNote = createNote(
					title = note.value.entity.title,
					text = note.value.entity.text,
					images = note.value.images,
					notebookId = folderId
				)
				return insertNote(newNote)
			}
			else {
				if (note.value != tempNote.value) {
					val updatedNote = createNote(
						title = note.value.entity.title,
						text = note.value.entity.text,
						images = note.value.images,
						notebookId = note.value.entity.notebookId,
						id = note.value.entity.id
					)
					return updateNote(updatedNote)
				}
			}
		}
		return null
	}

	private fun createNote(title: String?, text: String, images: List<ImageEntity>, notebookId: Int, id: Long = 0): Note {
		return Note(
			entity = NoteEntity(
				id = id,
				title = title,
				text = text,
				dateModification = ZonedDateTime.now(),
				urls = extractUrls(text),
				notebookId = notebookId
			),
			images = images
		)
	}

	private suspend fun insertNote(note: Note): Result {
		return withContext(viewModelScope.coroutineContext) {
			insertNoteWithImagesUseCase(note.entity, note.images)
			Result.SuccesfulInsert
		}
	}

	private suspend fun updateNote(note: Note): Result {
		return withContext(viewModelScope.coroutineContext) {
			updateNoteUseCase(note.entity, note.images)
			Result.SuccesfulUpdate
		}
	}

	private suspend fun copyUri(context: Context, uri: Uri): Uri {
		val extension = context.getUriExtension(uri) ?: "jpeg"
		val fileName = buildString {
			append("IMG_")
			append(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()))
			append("_${(0..999).random()}.$extension")
		}
		val fullPath = ImageStorageManager.saveImage(context, uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(context, AUTHORITY, file)
	}

	private companion object {
		private const val AUTHORITY = "net.azurewebsites.noties"
	}
}