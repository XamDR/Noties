package net.azurewebsites.noties.ui.notes

interface NotesMenuListener {
	fun showSortNotesDialog()
	fun changeNotesLayout(layoutType: LayoutType)
}