package net.azurewebsites.noties.domain

import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.data.NotebookDao
import net.azurewebsites.noties.data.NoteDao
import javax.inject.Inject

class InsertNotebookUseCase @Inject constructor(private val dao: NotebookDao) {
	suspend operator fun invoke(folder: NotebookEntity) = dao.insertFolder(folder)
}

class GetNotebooksUseCase @Inject constructor(private val dao: NotebookDao) {
	operator fun invoke() = dao.getFolders()
}

class UpdateNotebookUseCase @Inject constructor(private val dao: NotebookDao) {
	suspend operator fun invoke(folder: NotebookEntity) = dao.updateFolder(folder)
}

class DeleteNotebookAndMoveNotesToTrashUseCase @Inject constructor(
	private val notebookDao: NotebookDao,
	private val noteDao: NoteDao) {

	suspend operator fun invoke(notebook: Notebook) {
		for (note in notebook.notes) {
			val trashedNote = note.copy(notebookId = -1, isTrashed = true)
			noteDao.updateNote(trashedNote)
			notebookDao.incrementNoteCount(notebookId = -1)
		}
		notebookDao.deleteFolders(listOf(notebook.entity))
	}
}