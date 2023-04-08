package io.github.xamdr.noties.ui.notebooks

import io.github.xamdr.noties.core.Notebook
import io.github.xamdr.noties.core.NotebookEntity

interface NotebookItemPopupMenuListener {
	fun showEditNotebookNameDialog(notebook: NotebookEntity)
	fun deleteNotebook(notebook: Notebook)
}