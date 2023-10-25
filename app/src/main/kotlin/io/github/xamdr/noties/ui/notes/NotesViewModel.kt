package io.github.xamdr.noties.ui.notes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.ArchiveNotesUseCase
import io.github.xamdr.noties.domain.usecase.DeleteNotesUseCase
import io.github.xamdr.noties.domain.usecase.EmptyTrashUseCase
import io.github.xamdr.noties.domain.usecase.GetAllNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetTrashedNotesUseCase
import io.github.xamdr.noties.domain.usecase.MoveNotesToTrashUseCase
import io.github.xamdr.noties.domain.usecase.RestoreNotesUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
	private val getAllNotesUseCase: GetAllNotesUseCase,
	private val getNotesUseCase: GetNotesUseCase,
	private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
	private val moveNotesToTrashUseCase: MoveNotesToTrashUseCase,
	private val restoreNotesUseCase: RestoreNotesUseCase,
	private val archiveNotesUseCase: ArchiveNotesUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val emptyTrashUseCase: EmptyTrashUseCase) : ViewModel() {

	fun getNotesByTag(tagName: String): Flow<List<Note>> =
		if (tagName.isEmpty()) getAllNotesUseCase() else getNotesUseCase(tagName)

	fun getTrashedNotes(): Flow<List<Note>> = getTrashedNotesUseCase()

	suspend fun moveNotesToTrash(notes: List<Note>) = moveNotesToTrashUseCase(notes)

	suspend fun restoreNotesFromTrash(notes: List<Note>) = restoreNotesUseCase(notes, fromTrash = true)
	suspend fun restoreNotesArchived(notes: List<Note>) = restoreNotesUseCase(notes, fromTrash = false)

	suspend fun archiveNotes(notes: List<Note>) = archiveNotesUseCase(notes)

	suspend fun emptyRecycleBin(notes: List<Note>): Int = emptyTrashUseCase(notes)

	suspend fun deleteNotes(notes: List<Note>) = deleteNotesUseCase(notes)
}
