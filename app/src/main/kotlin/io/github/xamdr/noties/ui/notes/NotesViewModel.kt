package io.github.xamdr.noties.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.*
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
	private val getNotesUseCase: GetNotesUseCase,
	private val getAllNotesUseCase: GetAllNotesUseCase,
	private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
	private val moveNotesToTrashUseCase: MoveNotesToTrashUseCase,
	private val restoreNotesUseCase: RestoreNotesUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val emptyTrashUseCase: EmptyTrashUseCase) : ViewModel() {

	fun getNotesByTag(tagName: String): LiveData<List<Note>> {
		return ((if (tagName.isEmpty()) getAllNotesUseCase() else getNotesUseCase(tagName)))
			.asLiveData()
	}

	fun getTrashedNotes(): LiveData<List<Note>> = getTrashedNotesUseCase().asLiveData()

	suspend fun moveNotesToTrash(notes: List<Note>) = moveNotesToTrashUseCase(notes)

	suspend fun restoreNotes(notes: List<Note>) = restoreNotesUseCase(notes)

	suspend fun emptyRecycleBin(notes: List<Note>): Int = emptyTrashUseCase(notes)

	suspend fun deleteNotes(notes: List<Note>) = deleteNotesUseCase(notes)
}
