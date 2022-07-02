package net.azurewebsites.noties.ui.notebooks

import net.azurewebsites.noties.core.NotebookEntity

interface EditNotebookNameListener {
	fun showEditNotebookNameDialog(notebook: NotebookEntity)
}