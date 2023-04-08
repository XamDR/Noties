package io.github.xamdr.noties.ui.notes

interface NotesMenuListener {
	fun showSortNotesDialog()
	fun changeNotesLayout(layoutType: LayoutType)
}