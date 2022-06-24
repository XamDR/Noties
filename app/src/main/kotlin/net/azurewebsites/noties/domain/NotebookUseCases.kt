package net.azurewebsites.noties.domain

import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.data.NotebookDao
import net.azurewebsites.noties.data.NoteDao
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
	private val noteDao: NoteDao) {

	suspend operator fun invoke(notebook: Notebook) {
		for (note in notebook.notes) {
			val trashedNote = note.copy(isTrashed = true)
			noteDao.updateNote(trashedNote)
		}
		notebookDao.deleteNotebooks(listOf(notebook.entity))
	}
}