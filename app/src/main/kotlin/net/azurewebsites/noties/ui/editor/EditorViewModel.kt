package net.azurewebsites.noties.ui.editor

import android.text.Editable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.domain.InsertNoteUseCase
import net.azurewebsites.noties.ui.helpers.extractUrls
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
	private val insertNoteUseCase: InsertNoteUseCase) : ViewModel() {

	private val uiState = MutableStateFlow(NoteUiState())
	val note = uiState.asStateFlow()

	fun updateContent(s: Editable) {
		uiState.update { uiState.value.copy(text = s.toString()) }
	}

	fun createNote(title: String?, text: String, images: List<ImageEntity>, folderId: Int, id: Long = 0): Note {
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

	fun insertNote(note: Note) {
		viewModelScope.launch { insertNoteUseCase(note.entity) }
	}
}

data class NoteUiState(
	val id: Int = 0,
	val title: String? = null,
	val text: String = String.Empty,
	val updateDate: ZonedDateTime = ZonedDateTime.now()
)
