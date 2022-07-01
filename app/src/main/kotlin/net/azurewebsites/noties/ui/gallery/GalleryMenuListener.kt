package net.azurewebsites.noties.ui.gallery

interface GalleryMenuListener {
	fun shareImage(position: Int)
	fun setImageAs(position: Int)
	fun printImage(position: Int)
	fun onBackPressed()
	val currentItem: Int
}