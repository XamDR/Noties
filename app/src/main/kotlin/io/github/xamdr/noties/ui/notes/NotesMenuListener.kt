package io.github.xamdr.noties.ui.notes

interface NotesMenuListener {
	fun navigateToTags()
	fun showSortNotesDialog()
	fun changeNotesLayout(layoutType: LayoutType)
}