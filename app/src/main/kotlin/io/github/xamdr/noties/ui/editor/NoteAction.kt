package io.github.xamdr.noties.ui.editor

sealed interface NoteAction {
	object InsertNote : NoteAction
	object UpdateNote : NoteAction
	object DeleteEmptyNote : NoteAction
	object NoAction: NoteAction
}