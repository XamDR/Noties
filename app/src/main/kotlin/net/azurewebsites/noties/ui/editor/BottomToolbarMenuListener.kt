package net.azurewebsites.noties.ui.editor

interface BottomToolbarMenuListener {
	fun showBottomSheetMenuDialog()
	fun shareContent()
	fun showBottomSheetColorDialog()
	fun showDeleteImagesDialog()
	fun openTextFile()
	fun hideTodoList()
}