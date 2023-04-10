package io.github.xamdr.noties.domain

import io.github.xamdr.noties.data.entity.NotebookEntityCrossRefLocal
import io.github.xamdr.noties.data.entity.NotebookEntityLocal
import io.github.xamdr.noties.data.NoteDao
import io.github.xamdr.noties.data.NotebookDao
import javax.inject.Inject

class CreateNotebookUseCase @Inject constructor(private val notebookDao: NotebookDao) {
	suspend operator fun invoke(notebook: NotebookEntityLocal) = notebookDao.insertNotebook(notebook)
}

class GetNotebooksUseCase @Inject constructor(private val notebookDao: NotebookDao) {
	operator fun invoke() = notebookDao.getNotebooks()
}

class UpdateNotebookUseCase @Inject constructor(private val notebookDao: NotebookDao) {
	suspend operator fun invoke(folder: NotebookEntityLocal) = notebookDao.updateNotebook(folder)
}

class DeleteNotebookAndMoveNotesToTrashUseCase @Inject constructor(
	private val notebookDao: NotebookDao,
	private val noteDao: NoteDao
) {

	suspend operator fun invoke(notebook: NotebookEntityCrossRefLocal) {
		for (note in notebook.notes) {
			val trashedNote = note.copy(isTrashed = true)
			noteDao.updateNote(trashedNote)
		}
		notebookDao.deleteNotebooks(listOf(notebook.entity))
	}
}

class GetNotebooksUseCaseAsync @Inject constructor(private val notebookDao: NotebookDao) {
	suspend operator fun invoke() = notebookDao.getNotebooksAsyc()
}