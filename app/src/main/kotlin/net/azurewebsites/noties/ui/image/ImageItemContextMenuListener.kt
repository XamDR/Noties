package net.azurewebsites.noties.ui.image

interface ImageItemContextMenuListener {
	fun copyImage(position: Int)
	fun addAltText(position: Int)
	fun deleteImage(position: Int)
}