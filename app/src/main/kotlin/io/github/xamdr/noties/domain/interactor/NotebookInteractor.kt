package io.github.xamdr.noties.domain.interactor

import io.github.xamdr.noties.data.repository.NotebookRepository
import io.github.xamdr.noties.domain.model.Notebook
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotebookInteractor @Inject constructor(private val repository: NotebookRepository) {

	suspend fun createNotebook(notebook: Notebook) = repository.createNotebook(notebook.toEntityLocal())

	suspend fun getNotebooks(): Flow<List<Notebook>> {
		throw NotImplementedError("")
	}

	suspend fun updateNotebook(notebook: Notebook) {

	}

	suspend fun deleteNotebookAndMoveNotesToTrash(notebook: Notebook) {

	}
}