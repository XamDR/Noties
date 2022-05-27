package net.azurewebsites.noties.ui.editor

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.domain.InsertNoteWithImagesUseCase
import net.azurewebsites.noties.ui.helpers.extractUrls
import net.azurewebsites.noties.ui.helpers.getUriExtension
import net.azurewebsites.noties.ui.helpers.getUriMimeType
import net.azurewebsites.noties.ui.media.ImageStorageManager
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val insertNoteWithImagesUseCase: InsertNoteWithImagesUseCase) : ViewModel() {

	private val _note = MutableStateFlow(NoteEntity())
	val note = _note.asStateFlow()

	private val _images = MutableStateFlow(emptyList<ImageEntity>())
	val images = _images.asLiveData()

	fun addImages(context: Context, uris: List<Uri>) {
		viewModelScope.launch {
			for (uri in uris) {
				val newUri = copyUri(context, uri)
				val image = ImageEntity(
					uri = newUri,
					mimeType = context.getUriMimeType(newUri),
					noteId = _note.value.id
				)
				_images.value += image
			}
		}
	}

	fun insertNote(directoryId: Int) {
		if (_note.value.text.isNotEmpty() || _images.value.isNotEmpty()) {
			val note = createNote(
				title = _note.value.title,
				text = _note.value.text,
				images = _images.value,
				folderId = directoryId
			)
			insertNote(note)
		}
	}

	private fun createNote(title: String?, text: String, images: List<ImageEntity>, folderId: Int, id: Long = 0): Note {
		return Note(
			entity = NoteEntity(
				id = id,
				title = title,
				text = text,
				dateModification = ZonedDateTime.now(),
				urls = extractUrls(text),
				folderId = folderId
			),
			images = images
		)
	}

	private fun insertNote(note: Note) {
		viewModelScope.launch { insertNoteWithImagesUseCase(note.entity, note.images) }
	}

	private suspend fun copyUri(context: Context, uri: Uri): Uri {
		val sufix = (0..999).random()
		val extension = context.getUriExtension(uri) ?: "jpeg"
		val fileName = "IMG_${DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())}_$sufix.$extension"
		val fullPath = ImageStorageManager.saveToInternalStorage(context, uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(context, AUTHORITY, file)
	}

	private companion object {
		private const val AUTHORITY = "net.azurewebsites.eznotes"
	}
}