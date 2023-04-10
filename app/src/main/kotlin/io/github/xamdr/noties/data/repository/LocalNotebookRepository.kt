package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.NotebookDao
import io.github.xamdr.noties.data.entity.NotebookEntityLocal

class LocalNotebookRepository(private val notebookDao: NotebookDao) : NotebookRepository {

	override suspend fun createNotebook(notebook: NotebookEntityLocal) {
		notebookDao.insertNotebook(notebook)
	}
}