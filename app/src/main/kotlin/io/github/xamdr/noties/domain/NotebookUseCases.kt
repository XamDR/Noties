package io.github.xamdr.noties.domain

import io.github.xamdr.noties.core.Notebook
import io.github.xamdr.noties.core.NotebookEntity
import io.github.xamdr.noties.data.NoteDao
import io.github.xamdr.noties.data.NotebookDao
import javax.inject.Inject

class CreateNotebookUseCase @Inject constructor(private val notebookDao: NotebookDao) {
	suspend operator fun invoke(notebook: NotebookEntity) = notebookDao.insertNotebook(notebook)
}

class GetNotebooksUseCase @Inject constructor(private val notebookDao: NotebookDao) {
	operator fun invoke() = notebookDao.getNotebooks()
}

class UpdateNotebookUseCase @Inject constructor(private val notebookDao: NotebookDao) {
	suspend operator fun invoke(folder: NotebookEntity) = notebookDao.updateNotebook(folder)
}

class DeleteNotebookAndMoveNotesToTrashUseCase @Inject constructor(
	private val notebookDao: NotebookDao,
	private val noteDao: NoteDao
) {

	suspend operator fun invoke(notebook: Notebook) {
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