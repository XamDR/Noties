package io.github.xamdr.noties.ui.editor

interface NoteContentListener {

	fun onNoteTextChanged(text: String)
	fun onLinkClicked(url: String)
	fun onNoteContentLoading()
	fun onNoteContentLoaded()
}