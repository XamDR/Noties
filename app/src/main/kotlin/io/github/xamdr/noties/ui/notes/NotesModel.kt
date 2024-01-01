package io.github.xamdr.noties.ui.notes

import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note

sealed class GridItem(open val id: Long) {
	data class Header(override val id: Long, val title: Int) : GridItem(id)
	data class NoteItem(val note: Note) : GridItem(note.id)
}

fun groupNotesByPinnedCondition(notes: List<Note>): List<GridItem> {
	val (first, second) = notes.partition { it.pinned }
	val firstList = if (first.isEmpty()) first.map { GridItem.NoteItem(it) }
		else listOf(GridItem.Header(id = 0, title = R.string.pinned_notes_header)) + first.map { GridItem.NoteItem(it) }
	val secondList = if (first.isEmpty()) second.map { GridItem.NoteItem(it) }
		else listOf(GridItem.Header(id = -1, title = R.string.other_notes_header)) + second.map { GridItem.NoteItem(it) }
	return firstList + secondList
}

fun groupNotesByNonArchivedCondition(notes: List<Note>): List<GridItem> {
	val (first, second) = notes.partition { it.archived.not() }
	val firstList = first.map { GridItem.NoteItem(it) }
	val secondList = if (second.isEmpty()) emptyList()
		else listOf(GridItem.Header(id = -1, title = R.string.archived_notes_header)) + second.map { GridItem.NoteItem(it) }
	return firstList + secondList
}
