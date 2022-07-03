package net.azurewebsites.noties.ui.notebooks

import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity

interface NotebookItemPopupMenuListener {
	fun showEditNotebookNameDialog(notebook: NotebookEntity)
	fun deleteNotebook(notebook: Notebook)
}