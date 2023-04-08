package io.github.xamdr.noties.ui.notes.selection

import io.github.xamdr.noties.core.Note

interface RecyclerViewActionModeListener {
	fun showDeleteNotesDialog(notes: List<Note>)
}