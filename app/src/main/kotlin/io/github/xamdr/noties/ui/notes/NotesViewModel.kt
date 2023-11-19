package io.github.xamdr.noties.ui.notes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.ArchiveNotesUseCase
import io.github.xamdr.noties.domain.usecase.DeleteNotesUseCase
import io.github.xamdr.noties.domain.usecase.EmptyTrashUseCase
import io.github.xamdr.noties.domain.usecase.GetAllNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetArchivedNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetNotesWithReminderUseCase
import io.github.xamdr.noties.domain.usecase.GetTrashedNotesUseCase
import io.github.xamdr.noties.domain.usecase.MoveNotesToTrashUseCase
import io.github.xamdr.noties.domain.usecase.RestoreNotesUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
	private val getAllNotesUseCase: GetAllNotesUseCase,
	private val getNotesUseCase: GetNotesUseCase,
	private val getNotesWithReminderUseCase: GetNotesWithReminderUseCase,
	private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
	private val getArchivedNotesUseCase: GetArchivedNotesUseCase,
	private val moveNotesToTrashUseCase: MoveNotesToTrashUseCase,
	private val archiveNotesUseCase: ArchiveNotesUseCase,
	private val restoreNotesUseCase: RestoreNotesUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val emptyTrashUseCase: EmptyTrashUseCase) : ViewModel() {

	fun getNotes(screen: Screen): Flow<List<Note>> {
		return when (screen.type) {
			ScreenType.Archived -> getArchivedNotesUseCase()
			ScreenType.Main -> getAllNotesUseCase()
			ScreenType.Protected -> TODO()
			ScreenType.Reminder -> getNotesWithReminderUseCase()
			ScreenType.Tag -> getNotesUseCase(screen.title)
			ScreenType.Trash -> getTrashedNotesUseCase()
		}
	}

	suspend fun moveNotesToTrash(notes: List<Note>) = moveNotesToTrashUseCase(notes)

	suspend fun restoreNotesFromTrash(notes: List<Note>) = restoreNotesUseCase(notes, fromTrash = true)

	suspend fun archiveNotes(notes: List<Note>) = archiveNotesUseCase(notes)

	suspend fun restoreNotesArchived(notes: List<Note>) = restoreNotesUseCase(notes, fromTrash = false)

	suspend fun deleteNotes(ids: List<Long>) = deleteNotesUseCase(ids)

	suspend fun emptyRecycleBin(): Int = emptyTrashUseCase()
}
