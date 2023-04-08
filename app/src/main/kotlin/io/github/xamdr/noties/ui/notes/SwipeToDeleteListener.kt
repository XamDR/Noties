package io.github.xamdr.noties.ui.notes

import io.github.xamdr.noties.core.NoteEntity

interface SwipeToDeleteListener {
	fun moveNoteToTrash(note: NoteEntity)
}