package net.azurewebsites.noties.ui.notes

import net.azurewebsites.noties.core.NoteEntity

interface SwipeToDeleteListener {
	fun moveNoteToTrash(note: NoteEntity)
}