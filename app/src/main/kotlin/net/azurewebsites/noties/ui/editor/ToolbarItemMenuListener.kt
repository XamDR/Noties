package net.azurewebsites.noties.ui.editor

interface ToolbarItemMenuListener {
	fun shareContent()
	fun showDeleteImagesDialog()
	fun openTextFile()
	fun hideTodoList()
	fun lockNote()
	fun unlockNote()
	fun pinNote()
	fun unpinNote()
}