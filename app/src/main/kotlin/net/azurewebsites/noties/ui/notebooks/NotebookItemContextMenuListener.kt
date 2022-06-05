package net.azurewebsites.noties.ui.notebooks

import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity

interface NotebookItemContextMenuListener {
	fun changeNotebookName(notebook: NotebookEntity)
	fun deleteNotebook(notebook: Notebook)
	fun lockNotebook(notebook: NotebookEntity)
}