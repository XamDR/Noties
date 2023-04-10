package io.github.xamdr.noties.ui.notebooks

import io.github.xamdr.noties.data.entity.NotebookEntityCrossRefLocal
import io.github.xamdr.noties.data.entity.NotebookEntityLocal

interface NotebookItemPopupMenuListener {
	fun showEditNotebookNameDialog(notebook: NotebookEntityLocal)
	fun deleteNotebook(notebook: NotebookEntityCrossRefLocal)
}