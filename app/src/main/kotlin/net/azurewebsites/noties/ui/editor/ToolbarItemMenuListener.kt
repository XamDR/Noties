package net.azurewebsites.noties.ui.editor

interface ToolbarItemMenuListener {
	fun lockNote()
	fun unlockNote()
	fun pinNote()
	fun unpinNote()
	fun shareContent()
	fun openTextFile()
	fun showDeleteImagesDialog()
	fun hideTodoList()
	fun showBottomSheetMenuDialog()
	fun showBottomSheetColorDialog()
}