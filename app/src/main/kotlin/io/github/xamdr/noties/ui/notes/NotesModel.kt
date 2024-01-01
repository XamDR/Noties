package io.github.xamdr.noties.ui.notes

import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note

sealed class GridItem(val id: Int) {
	data class Header(val title: Int) : GridItem(title.hashCode())
	data class NoteItem(val note: Note) : GridItem(note.id.toInt())
}

fun groupNotesByCondition(notes: List<Note>, screenType: ScreenType): List<GridItem> {
	return when (screenType) {
		ScreenType.Main, ScreenType.Protected, ScreenType.Reminder -> groupNotesByPinnedCondition(notes)
		ScreenType.Tag -> groupNotesByNonArchivedCondition(notes)
		ScreenType.Archived, ScreenType.Trash -> notes.map { GridItem.NoteItem(it) }
	}
}

private fun groupNotesByPinnedCondition(notes: List<Note>): List<GridItem> {
	val (pinned, nonPinned) = notes.partition { it.pinned }
	val firstList = if (pinned.isEmpty()) pinned.map { GridItem.NoteItem(it) }
		else listOf(GridItem.Header(title = R.string.pinned_notes_header)) + pinned.map { GridItem.NoteItem(it) }
	val secondList = if (pinned.isEmpty()) nonPinned.map { GridItem.NoteItem(it) }
		else listOf(GridItem.Header(title = R.string.other_notes_header)) + nonPinned.map { GridItem.NoteItem(it) }
	return firstList + secondList
}

private fun groupNotesByNonArchivedCondition(notes: List<Note>): List<GridItem> {
	val (nonArchived, archived) = notes.partition { it.archived.not() }
	val (pinned, nonPinned) = nonArchived.partition { it.pinned }
	val firstListPinned = if (pinned.isEmpty()) emptyList() else
		listOf(GridItem.Header(title = R.string.pinned_notes_header)) + pinned.map { GridItem.NoteItem(it) }
	val firstListNonPinned = if (pinned.isEmpty()) nonPinned.map { GridItem.NoteItem(it) } else
		listOf(GridItem.Header(title = R.string.other_notes_header)) + nonPinned.map { GridItem.NoteItem(it) }
	val secondList = if (archived.isEmpty()) emptyList()
		else listOf(GridItem.Header(title = R.string.archived_notes_header)) + archived.map { GridItem.NoteItem(it) }
	return firstListPinned + firstListNonPinned + secondList
}
