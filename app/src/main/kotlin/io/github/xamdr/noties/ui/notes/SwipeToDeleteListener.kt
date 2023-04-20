package io.github.xamdr.noties.ui.notes

import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity

interface SwipeToDeleteListener {
	fun moveNoteToTrash(note: DatabaseNoteEntity)
}