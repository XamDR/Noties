package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.entity.NotebookEntityLocal

interface NotebookRepository {

	suspend fun createNotebook(notebook: NotebookEntityLocal)
}