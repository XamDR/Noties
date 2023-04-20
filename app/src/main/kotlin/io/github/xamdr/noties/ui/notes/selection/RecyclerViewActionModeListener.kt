package io.github.xamdr.noties.ui.notes.selection

import io.github.xamdr.noties.domain.model.Note

interface RecyclerViewActionModeListener {
	fun showDeleteNotesDialog(notes: List<Note>)
}