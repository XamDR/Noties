package io.github.xamdr.noties.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.domain.usecase.DeleteNotesUseCase
import io.github.xamdr.noties.domain.usecase.EmptyTrashUseCase
import io.github.xamdr.noties.domain.usecase.GetAllNotesUseCase
import io.github.xamdr.noties.domain.usecase.GetTagsUseCase
import io.github.xamdr.noties.domain.usecase.GetTrashedNotesUseCase
import io.github.xamdr.noties.domain.usecase.MoveNotesToTrashUseCase
import io.github.xamdr.noties.domain.usecase.RestoreNotesUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
	private val getAllNotesUseCase: GetAllNotesUseCase,
	private val getTagsUseCase: GetTagsUseCase,
	private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
	private val moveNotesToTrashUseCase: MoveNotesToTrashUseCase,
	private val restoreNotesUseCase: RestoreNotesUseCase,
	private val deleteNotesUseCase: DeleteNotesUseCase,
	private val emptyTrashUseCase: EmptyTrashUseCase) : ViewModel() {

	fun getAllNotes(): Flow<List<Note>> = getAllNotesUseCase()

	fun getTags(): Flow<List<Tag>> = getTagsUseCase()

	fun getTrashedNotes(): LiveData<List<Note>> = getTrashedNotesUseCase().asLiveData()

	suspend fun moveNotesToTrash(notes: List<Note>) = moveNotesToTrashUseCase(notes)

	suspend fun restoreNotes(notes: List<Note>) = restoreNotesUseCase(notes)

	suspend fun emptyRecycleBin(notes: List<Note>): Int = emptyTrashUseCase(notes)

	suspend fun deleteNotes(notes: List<Note>) = deleteNotesUseCase(notes)
}
