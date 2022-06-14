package net.azurewebsites.noties.ui.notes.selection

import net.azurewebsites.noties.core.Note

interface RecyclerViewActionModeListener {
	fun showDeleteNotesDialog(notes: List<Note>)
}